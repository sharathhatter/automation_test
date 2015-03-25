package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.handler.AnalyticsIdentifierKeys;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public abstract class SocialLoginActivity extends FacebookAndGPlusSigninBaseActivity {

    public void setUpSocialButtons(Button btnGoogleLogin, Button btnFacebookLoginButton) {
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternetConnection()) {
                    handler.sendOfflineError();
                    return;
                }
                // Don't offer G+ sign in if the app's version is too low to support Google Play
                // Services.
                if (supportsGooglePlayServices()) {
                    initializeGooglePlusSignIn();
                    logSignInBtnClickEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE);
                    signInViaGPlus();
                } else {
                    // Show update dialog
                    showAlertDialog(getString(R.string.updateGooglePlayServices), getString(R.string.updateGooglePlayServicesDesc),
                            DialogButton.OK, DialogButton.CANCEL, Constants.GOOGLE_PLAY_SERVICES, null, getString(R.string.update));
                }
            }
        });
        btnFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternetConnection()) {
                    handler.sendOfflineError();
                    return;
                }
                initializeFacebookLogin();
            }
        });
    }

    public void logSignInBtnClickEvent(String type) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.TYPE, type);
        trackEvent(TrackingAware.LOGIN_BTN_CLICKED, eventAttribs);
    }

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    protected boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCurrentActivity()) ==
                ConnectionResult.SUCCESS;
    }

    @Override
    protected void onPlusClientSignIn(String email, Person person) {
        if (mIsInLogoutMode) {
            hideProgressDialog();
            signOutFromGplus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showAlertDialog("Email required", "Unable to get your email-address\n" +
                    "Please check your privacy settings.", Constants.SOCIAL_LOGOUT, SocialAccount.GP);
            return;
        }
        if (person == null) {
            showAlertDialog(null, "Unable to read your profile information\n" +
                    "Please check your privacy settings.", Constants.SOCIAL_LOGOUT, SocialAccount.GP);
            return;
        }

        String displayName = person.getDisplayName();
        String firstName = null;
        String lastName = null;
        String imgUrl = null;
        if (person.getName() != null) {
            firstName = person.getName().getGivenName();
            lastName = person.getName().getFamilyName();
        }

        String gender = person.getGender() == Person.Gender.FEMALE ? "female" : "male";
        String profileLink = person.getUrl();
        boolean isVerified = person.isVerified();
        String uid = person.getId();
        if (person.getImage() != null) {
            imgUrl = person.getImage().getUrl();
        }

        SocialAccount socialAccount = new SocialAccount(email, displayName, gender, profileLink, uid,
                isVerified, firstName, lastName, imgUrl);

        startSocialLogin(SocialAccount.GP, socialAccount);
    }

    private void startSocialLogin(String loginType, SocialAccount socialAccount) {
        showProgressDialog(getString(R.string.please_wait));
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Gson gson = new Gson();
        bigBasketApiService.socialLogin(loginType, gson.toJson(socialAccount, SocialAccount.class),
                new LoginApiResponseCallback(socialAccount.getEmail(),
                        null, false, loginType, socialAccount, false));
    }

    @Override
    public void onFacebookSignIn(Session facebookSession) {
        if (mIsInLogoutMode) {
            doLogout();
            return;
        }
        showProgressDialog(getString(R.string.please_wait));
        Request facebookUserDetailRequest = Request.newMeRequest(facebookSession, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                hideProgressDialog();
                if (user != null) {
                    Object emailObj = response.getGraphObject().getProperty(Constants.EMAIL);
                    String email = emailObj != null ? emailObj.toString() : null;
                    if (TextUtils.isEmpty(email)) {
                        showAlertDialog("Email required", "Unable to get your email-address\n" +
                                "Please check your privacy settings.", Constants.SOCIAL_LOGOUT, SocialAccount.FB);
                        return;
                    }
                    String firstName = user.getFirstName();
                    String lastName = user.getLastName();
                    String gender = null;
                    try {
                        gender = user.getInnerJSONObject().getString(Constants.FB_GENDER);
                    } catch (JSONException e) {

                    }
                    String profileLink = user.getLink();
                    String imgUrl = "http://graph.facebook.com/" + user.getId() + "/picture?type=large";
                    boolean isVerified = false;
                    try {
                        isVerified = user.getInnerJSONObject().getString(Constants.FB_VERIFIED).equalsIgnoreCase("verified");
                    } catch (JSONException e) {

                    }
                    String uid = user.getId();
                    SocialAccount socialAccount = new SocialAccount(email, firstName, gender, profileLink, uid,
                            isVerified, firstName, lastName, imgUrl);
                    startSocialLogin(SocialAccount.FB, socialAccount);
                } else if (response.getError() != null) {
                    showAlertDialog(null, response.getError().getErrorMessage());
                }
            }
        });
        Request.executeBatchAsync(facebookUserDetailRequest);
    }

    @Override
    protected void onPlusClientRevokeAccess() {
        showProgressDialog(getString(R.string.please_wait));
        initializeGooglePlusSignIn();
    }

    @Override
    protected void onPlusClientBlockingUI(boolean show) {
        if (show) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    protected void onPlusClientSignOut() {
        doLogout();
    }

    @Override
    protected void updatePlusConnectedButtonState() {
        if (getPlusClient() == null) return;

        boolean connected = getPlusClient().isConnected();
        if (connected) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onFacebookSignOut() {
        doLogout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == Constants.SOCIAL_ACCOUNT_NOT_LINKED && data != null) {
            String loginType = data.getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
            if (!TextUtils.isEmpty(loginType)) {
                switch (loginType) {
                    case SocialAccount.GP:
                        revokeGPlusAccess();
                        break;
                    case SocialAccount.FB:
                        revokeFbAccess();
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.GOOGLE_PLAY_SERVICES:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                    }
                    break;
                case Constants.SOCIAL_LOGOUT:
                    if (valuePassed != null) {
                        if (valuePassed.equals(SocialAccount.GP)) {
                            signOutFromGplus();
                        } else if (valuePassed.equals(SocialAccount.FB)) {
                            revokeFbAccess();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void revokeGPlusAccess() {
        showProgressDialog(getString(R.string.please_wait));
        super.revokeGPlusAccess();
    }

    public void onLogoutRequested() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String socialAccountType = preferences.getString(Constants.SOCIAL_ACCOUNT_TYPE, "");
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccount.getSocialLoginTypes().contains(socialAccountType)) {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            switch (socialAccountType) {
                case SocialAccount.FB:
                    mIsInLogoutMode = true;
                    Session session = Session.getActiveSession();
                    if (session != null) {
                        if (!session.isClosed()) {
                            session.closeAndClearTokenInformation();
                        }
                    } else {
                        session = new Session(getCurrentActivity());
                        Session.setActiveSession(session);
                        session.closeAndClearTokenInformation();
                    }
                    doLogout();
                    break;
                case SocialAccount.GP:
                    mIsInLogoutMode = true;
                    initializeGooglePlusSignIn();
                    showProgressDialog(getString(R.string.please_wait));
                    initiatePlusClientConnect();
                    break;
            }
        } else {
            doLogout();
        }
    }

    public void doLogout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.FIRST_NAME);
        editor.remove(Constants.BBTOKEN_KEY);
        editor.remove(Constants.OLD_BBTOKEN_KEY);
        editor.remove(Constants.MID_KEY);
        editor.remove(Constants.MEMBER_FULL_NAME_KEY);
        editor.remove(Constants.MEMBER_EMAIL_KEY);
        editor.remove(Constants.SOCIAL_ACCOUNT_TYPE);
        editor.remove(Constants.HAS_USER_CHOSEN_CITY);
        editor.commit();
        AuthParameters.updateInstance(getCurrentActivity());

        String analyticsAdditionalAttrsJson = preferences.getString(Constants.ANALYTICS_ADDITIONAL_ATTRS, null);
        editor.remove(Constants.ANALYTICS_ADDITIONAL_ATTRS);

        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_NAME, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_HUB, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_BDAY, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_GENDER, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, null);

        if (!TextUtils.isEmpty(analyticsAdditionalAttrsJson)) {
            Gson gson = new Gson();
            HashMap<String, Object> additionalAttrMap = new HashMap<>();
            additionalAttrMap = (HashMap<String, Object>) gson.fromJson(analyticsAdditionalAttrsJson, additionalAttrMap.getClass());
            if (additionalAttrMap != null) {
                for (Map.Entry<String, Object> entry : additionalAttrMap.entrySet()) {
                    LocalyticsWrapper.setIdentifier(entry.getKey(), null);
                }
            }
        }
        mIsInLogoutMode = false;
        goToHome(true);
    }

    public void saveLoginUserDetailInPreference(LoginApiResponse loginApiResponse, String socialAccountType,
                                                String email, String password, boolean rememberMe) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.BBTOKEN_KEY, loginApiResponse.bbToken);
        editor.putString(Constants.MEMBER_EMAIL_KEY, email);
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccount.getSocialLoginTypes().contains(socialAccountType)) {
            editor.putString(Constants.SOCIAL_ACCOUNT_TYPE, socialAccountType);
        } else {
            editor.remove(Constants.SOCIAL_ACCOUNT_TYPE);
        }
        if (rememberMe && !TextUtils.isEmpty(password)) {
            editor.putString(Constants.EMAIL_PREF, email);
            editor.putBoolean(Constants.REMEMBER_ME_PREF, true);
            editor.putString(Constants.PASSWD_PREF, password);
        } else {
            editor.remove(Constants.EMAIL_PREF);
            editor.remove(Constants.REMEMBER_ME_PREF);
            editor.remove(Constants.PASSWD_PREF);
        }
        editor.commit();

        UIUtil.updateStoredUserDetails(getCurrentActivity(),
                loginApiResponse.userDetails, email, loginApiResponse.mId);
        onLoginSuccess();
    }

    public void onLoginSuccess() {
        SectionManager.clearAllSectionData(getCurrentActivity());
        String deepLink = getIntent().getStringExtra(Constants.DEEP_LINK);
        if (!TextUtils.isEmpty(deepLink)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
            editor.putString(Constants.DEEP_LINK, deepLink);
            editor.commit();
        }
        goToHome(true);
    }


    public class LoginApiResponseCallback implements Callback<LoginApiResponse> {

        private String loginType;
        private String email;
        private String password;
        private boolean rememberMe, isNewAccount;
        private SocialAccount socialAccount;

        public LoginApiResponseCallback(String email, String password, boolean rememberMe, String loginType) {
            this.email = email;
            this.password = password;
            this.rememberMe = rememberMe;
            this.loginType = loginType;
        }

        public LoginApiResponseCallback(String email, String password, boolean rememberMe, String loginType,
                                        SocialAccount socialAccount, boolean isNewAccount) {
            this(email, password, rememberMe, loginType);
            this.loginType = loginType;
            this.socialAccount = socialAccount;
            this.isNewAccount = isNewAccount;
        }

        @Override
        public void success(LoginApiResponse loginApiResponse, retrofit.client.Response response) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (loginApiResponse.status) {
                case Constants.OK:
                    saveLoginUserDetailInPreference(loginApiResponse, loginType,
                            email, password, rememberMe);
                    /*
                    switch (loginType) {
                        case SocialAccount.FB:
                            trackEventNewAccount(TrackingAware.MY_ACCOUNT_FACEBOOK_LOGIN_SUCCESS,
                                    email, isNewAccount);
                            break;
                        case SocialAccount.GP:
                            trackEventNewAccount(TrackingAware.MY_ACCOUNT_GOOGLE_LOGIN_SUCCESS,
                                    email, isNewAccount);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            trackEvent(TrackingAware.MY_ACCOUNT_LOGIN_SUCCESS, null);
                            break;
                        case Constants.REGISTER_ACCOUNT_TYPE:
                            showToast(getString(R.string.thanksForRegistering));
                            trackEvent(TrackingAware.MY_ACCOUNT_REGISTRATION_SUCCESS, null);
                            break;
                        default:
                            throw new AssertionError("Login or register type error while success(status=OK)");
                    }
                    */
                    break;
                case Constants.ERROR:
                    switch (loginApiResponse.getErrorTypeAsInt()) {
                        case ApiErrorCodes.INVALID_USER_PASSED:
                            showAlertDialog(null, getString(R.string.INVALID_USER_PASS));
                            break;
                        case ApiErrorCodes.NO_ACCOUNT:
                            Intent intent = new Intent(getCurrentActivity(), SocialLoginConfirmActivity.class);
                            intent.putExtra(Constants.SOCIAL_LOGIN_PARAMS, socialAccount);
                            intent.putExtra(Constants.SOCIAL_LOGIN_TYPE, loginType);
                            startActivityForResult(intent, Constants.SOCIAL_ACCOUNT_NOT_LINKED);
                            break;
                        case ApiErrorCodes.INVALID_REFERRAL_CODE:
                            showAlertDialog(null, loginApiResponse.message);
                            break;
                        default:
                            handler.sendEmptyMessage(loginApiResponse.getErrorTypeAsInt(),
                                    loginApiResponse.message);
                            break;
                    }

                    switch (loginType) {
                        case SocialAccount.FB:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, loginApiResponse.message);
                            break;
                        case SocialAccount.GP:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, loginApiResponse.message);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, loginApiResponse.message);
                            break;
                        case Constants.REGISTER_ACCOUNT_TYPE:
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TrackEventkeys.FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.REGISTRATION_FAILED, map);
                            break;
                        default:
                            throw new AssertionError("Login or register type error while success(status=ERROR)");
                    }
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            handler.handleRetrofitError(error);
            switch (loginType) {
                case SocialAccount.FB:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, error.toString());
                    break;
                case SocialAccount.GP:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, error.toString());
                    break;
                case Constants.SIGN_IN_ACCOUNT_TYPE:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_NORMAL, error.toString());
                    break;
                case Constants.REGISTER_ACCOUNT_TYPE:
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.FAILURE_REASON, error.toString());
                    trackEvent(TrackingAware.REGISTRATION_FAILED, map);
                    break;
                default:
                    throw new AssertionError("Login or register type error while success(status=ERROR)");
            }
        }
    }

    private void logSignInFailureEvent(String type, String reason) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.FAILURE_REASON, reason);
        map.put(TrackEventkeys.TYPE, type);
        trackEvent(TrackingAware.LOGIN_FAILED, map);
    }
}
