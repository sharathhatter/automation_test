package com.bigbasket.mobileapp.activity.account.uiv3;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.handler.AnalyticsIdentifierKeys;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.managers.SectionManager;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.SocialAccountType;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public abstract class SocialLoginActivity extends FacebookAndGPlusSigninBaseActivity {

    public void setUpSocialButtons(View btnGoogleLogin, View btnFacebookLoginButton) {
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternetConnection()) {
                    handler.sendOfflineError();
                    return;
                }
                // Don't offer G+ sign in if the app's version is too low to support Google Play
                // Services.
                int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCurrentActivity());
                switch (playServicesAvailable) {
                    case ConnectionResult.SUCCESS:
                        initializeGooglePlusSignIn();
                        logSignInBtnClickEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE);
                        signInViaGPlus();
                        break;
                    default:
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playServicesAvailable,
                                getCurrentActivity(), NavigationCodes.GO_TO_HOME);
                        dialog.show();
                        break;
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
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        trackEvent(TrackingAware.LOGIN_BTN_CLICKED, eventAttribs);
    }

    @Override
    protected void onPlusClientSignIn(String authToken) {
        if (mIsInLogoutMode) {
            hideProgressDialog();
            signOutFromGplus();
            return;
        }
        startSocialLogin(SocialAccountType.GP, authToken);
    }

    private void startSocialLogin(String loginType, String authToken) {
        showProgressDialog(getString(R.string.please_wait));
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        bigBasketApiService.socialLogin(loginType, authToken,
                new LoginApiResponseCallback(null,
                        null, false, loginType, authToken));
    }

    @Override
    public void onFacebookSignIn(AccessToken accessToken) {
        if (mIsInLogoutMode) {
            doLogout();
            return;
        }
        startSocialLogin(SocialAccountType.FB, accessToken.getToken());
    }

    @Override
    protected void onPlusClientRevokeAccess() {
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
        if (getPlusClient() == null || isSuspended()) return;

        boolean connected = getPlusClient().isConnected();
        if (connected) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == Constants.SOCIAL_ACCOUNT_NOT_LINKED && data != null) {
            String loginType = data.getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
            if (!TextUtils.isEmpty(loginType)) {
                switch (loginType) {
                    case SocialAccountType.GP:
                        revokeGPlusAccess();
                        break;
                    case SocialAccountType.FB:
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
                        if (valuePassed.equals(SocialAccountType.GP)) {
                            signOutFromGplus();
                        } else if (valuePassed.equals(SocialAccountType.FB)) {
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
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccountType.getSocialLoginTypes().contains(socialAccountType)) {
            switch (socialAccountType) {
                case SocialAccountType.FB:
                    mIsInLogoutMode = true;
                    LoginManager.getInstance().logOut();
                    doLogout();
                    break;
                case SocialAccountType.GP:
                    mIsInLogoutMode = true;
                    if (UIUtil.isPhoneWithGoogleAccount(this)) {
                        initializeGooglePlusSignIn();
                        showProgressDialog(getString(R.string.please_wait));
                        initiatePlusClientConnect();
                    } else {
                        doLogout();
                    }
                    break;
            }
        } else {
            doLogout();
        }
    }

    @SuppressWarnings("unchecked")
    public void doLogout() {
        SectionManager.clearAllSectionData(getCurrentActivity());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.FIRST_NAME_PREF);
        editor.remove(Constants.BBTOKEN_KEY);
        editor.remove(Constants.OLD_BBTOKEN_KEY);
        editor.remove(Constants.MID_KEY);
        editor.remove(Constants.MEMBER_FULL_NAME_KEY);
        editor.remove(Constants.MEMBER_EMAIL_KEY);
        editor.remove(Constants.SOCIAL_ACCOUNT_TYPE);
        editor.remove(Constants.UPDATE_PROFILE_IMG_URL);
        editor.remove(Constants.IS_KIRANA);
        editor.commit();
        AuthParameters.reset();
        AppDataDynamic.reset(getCurrentActivity());

        MoEngageWrapper.setUserAttribute(moEHelper, Constants.IS_LOGGED_IN, false);

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
        moEHelper.logoutUser();
        mIsInLogoutMode = false;
        goToHome(true);
    }

    public void saveLoginUserDetailInPreference(LoginApiResponse loginApiResponse, String socialAccountType,
                                                String email, String password, boolean rememberMe) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.BBTOKEN_KEY, loginApiResponse.bbToken);
        editor.putString(Constants.MEMBER_EMAIL_KEY, email);
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccountType.getSocialLoginTypes().contains(socialAccountType)) {
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
        editor.apply();

        UIUtil.updateStoredUserDetails(getCurrentActivity(),
                loginApiResponse.userDetails, email, loginApiResponse.mId);
        onLoginSuccess();
    }

    public void onLoginSuccess() {
        String deepLink = getIntent().getStringExtra(Constants.DEEP_LINK);
        if (!TextUtils.isEmpty(deepLink)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
            editor.putString(Constants.DEEP_LINK, deepLink);
            editor.apply();
        } else {
            String fragmentCode = getIntent().getStringExtra(Constants.FRAGMENT_CODE);
            if (!TextUtils.isEmpty(fragmentCode)) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
                editor.putString(Constants.FRAGMENT_CODE, fragmentCode);
                editor.apply();
            }
        }
        goToHome(true);
    }

    private void logSignInFailureEvent(String type, String reason) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.FAILURE_REASON, reason);
        map.put(TrackEventkeys.TYPE, type);
        trackEvent(TrackingAware.LOGIN_FAILED, map);
    }

    public class LoginApiResponseCallback implements Callback<ApiResponse<LoginApiResponse>> {

        private String loginType;
        private String email;
        private String password;
        private boolean rememberMe;
        private String authToken;

        public LoginApiResponseCallback(String email, String password, boolean rememberMe,
                                        String loginType,
                                        @Nullable String authToken) {
            this.email = email;
            this.password = password;
            this.rememberMe = rememberMe;
            this.loginType = loginType;
            this.authToken = authToken;
        }

        @Override
        public void success(ApiResponse<LoginApiResponse> loginApiResponse, retrofit.client.Response response) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (loginApiResponse.status) {
                case 0:
                    if (TextUtils.isEmpty(email)) {
                        email = loginApiResponse.apiResponseContent.email;
                    }
                    saveLoginUserDetailInPreference(loginApiResponse.apiResponseContent, loginType,
                            email, password, rememberMe);
                    break;
                case ApiErrorCodes.NO_ACCOUNT:
                    Intent intent = new Intent(getCurrentActivity(), SocialLoginConfirmActivity.class);
                    intent.putExtra(Constants.SOCIAL_LOGIN_TYPE, loginType);
                    intent.putExtra(Constants.AUTH_TOKEN, authToken);
                    setNextScreenNavigationContext(TrackEventkeys.NC_SIGNUP_SCREEN);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case ApiErrorCodes.INVALID_USER_PASSED:
                    showAlertDialog(null, getString(R.string.INVALID_USER_PASS));
                    break;
                default:
                    handler.sendEmptyMessage(loginApiResponse.status,
                            loginApiResponse.message);
                    switch (loginType) {
                        case SocialAccountType.FB:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, loginApiResponse.message);
                            break;
                        case SocialAccountType.GP:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, loginApiResponse.message);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_NORMAL, loginApiResponse.message);
                            break;
                        case Constants.REGISTER_ACCOUNT_TYPE:
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TrackEventkeys.FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.REGISTRATION_FAILED, map);
                            break;
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
                case SocialAccountType.FB:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, error.toString());
                    break;
                case SocialAccountType.GP:
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
}
