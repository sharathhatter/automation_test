package com.bigbasket.mobileapp.activity.account.uiv3;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.fragment.dialogs.ConfirmationDialogFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.OnLogoutListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.SocialAccountType;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.LogoutTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public abstract class SocialLoginActivity extends FacebookAndGPlusSigninBaseActivity
        implements OnLogoutListener {

    private boolean mIsInLogoutMode;

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
                        //initializeGoogleApiClient();
                        logSignInBtnClickEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE);
                        showProgressDialog(getString(R.string.please_wait));
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
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.LOGIN_BTN_CLICKED, eventAttribs);
    }

    @Override
    protected void onPlusClientSignIn(String authToken) {
        startSocialLogin(SocialAccountType.GP, authToken);
    }

    @Override
    protected void onPlusClientSignInFailed() {
        hideProgressView();
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
        } else {
            showToast(getString(R.string.common_google_play_services_sign_in_failed_title));
        }
    }

    private void startSocialLogin(String loginType, String authToken) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);

        Call<ApiResponse<LoginApiResponse>> call = bigBasketApiService.socialLogin(loginType, authToken);
        call.enqueue(new LoginApiResponseCallback(null, null, false, loginType, authToken));
    }

    @Override
    public void onFacebookSignIn(AccessToken accessToken) {
        if (mIsInLogoutMode) {
            doLogout(true);
            return;
        }
        showProgressDialog(getString(R.string.please_wait));
        startSocialLogin(SocialAccountType.FB, accessToken.getToken());
    }

    @Override
    protected void onPlusClientSignOut() {
        //hideProgressDialog();
        doLogout(true);
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
/*
    //Looks like Constants.SOCIAL_LOGOUT dialog is not used at all
    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case Constants.SOCIAL_LOGOUT:
                if (valuePassed != null) {
                    if (valuePassed.equals(SocialAccountType.GP)) {
                        showProgressDialog(getString(R.string.please_wait));
                        signOutFromGplus();
                    } else if (valuePassed.equals(SocialAccountType.FB)) {
                        revokeFbAccess();
                    }
                }
                break;
            default:
                super.onPositiveButtonClicked(sourceName, valuePassed);
        }
    }
*/

    @Override
    protected void revokeGPlusAccess() {
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
        showProgressDialog(getString(R.string.please_wait));
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccountType.getSocialLoginTypes().contains(socialAccountType)) {
            switch (socialAccountType) {
                case SocialAccountType.FB:
                    mIsInLogoutMode = true;
                    LoginManager.getInstance().logOut();
                    doLogout(true);
                    break;
                case SocialAccountType.GP:
                    mIsInLogoutMode = true;
                    if (UIUtil.isPhoneWithGoogleAccount(this)) {
                        signOutFromGplus();
                    } else {
                        doLogout(true);
                    }
                    break;
            }
        } else {
            doLogout(false);
        }
    }

    /**
     * Return true to consume logout event and block defaut post logout operation
     */
    protected void postLogout(boolean success) {
        Toast.makeText(getCurrentActivity(), getString(R.string.loggedOut),
                Toast.LENGTH_SHORT).show();
    }

    public void doLogout(boolean wasSocialLogin) {
        if (wasSocialLogin) {
            PreferenceManager.getDefaultSharedPreferences(getCurrentActivity())
                    .edit()
                    .remove(Constants.SOCIAL_ACCOUNT_TYPE)
                    .commit();
        }
        if(!AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            LogoutTask logoutTask = new LogoutTask(this);
            logoutTask.execute();
        } else {
            onLogoutSuccess();
        }
    }

    @Override
    public void onLogoutSuccess() {
        moEHelper.logoutUser();
        mIsInLogoutMode = false;
        if (isSuspended()) return;
        try {
            hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        postLogout(true);
    }

    @Override
    public void onLogoutFailure(ErrorResponse errorResponse) {
        if (isSuspended()) return;
        try {
            hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        if (errorResponse.isException()) {
            handler.handleRetrofitError(errorResponse.getThrowable(), false);
        } else if (errorResponse.getErrorType() == ErrorResponse.HTTP_ERROR) {
            handler.handleHttpError(errorResponse.getCode(), errorResponse.getMessage(),
                    false);
        } else {
            handler.sendEmptyMessage(errorResponse.getCode(), errorResponse.getMessage());
        }
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
            int fragmentCode;
            try {
                fragmentCode = getIntent().getIntExtra(Constants.FRAGMENT_CODE, -1);
            } catch (ClassCastException e) {
                // Defensive catch
                fragmentCode = -1;
            }
            if (fragmentCode > -1) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
                editor.putInt(Constants.FRAGMENT_CODE, fragmentCode);
                editor.apply();
            }
        }
        boolean shouldGoToHome = getIntent().getBooleanExtra(Constants.GO_TO_HOME, true);
        if (shouldGoToHome) {
            goToHome();
        } else {
            if (getCurrentActivity() instanceof CartInfoAware) {
                ((CartInfoAware) getCurrentActivity()).markBasketDirty();
            }
            setResult(NavigationCodes.BASKET_CHANGED);
            finish();
        }
    }

    private void logSignInFailureEvent(String type, String reason) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.FAILURE_REASON, reason);
        map.put(TrackEventkeys.TYPE, type);
        trackEvent(TrackingAware.LOGIN_FAILED, map);
    }

    public class LoginApiResponseCallback extends BBNetworkCallback<ApiResponse<LoginApiResponse>> {

        private String loginType;
        private String email;
        private String password;
        private boolean rememberMe;
        private String authToken;

        public LoginApiResponseCallback(String email, String password, boolean rememberMe,
                                        String loginType,
                                        @Nullable String authToken) {
            super(getCurrentActivity());
            this.email = email;
            this.password = password;
            this.rememberMe = rememberMe;
            this.loginType = loginType;
            this.authToken = authToken;
        }

        @Override
        public void onSuccess(ApiResponse<LoginApiResponse> loginApiResponse) {
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
                    setCurrentScreenName(TrackEventkeys.NC_SIGNUP_SCREEN);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case ApiErrorCodes.INVALID_USER_PASSED:
                    //showAlertDialog(null, getString(R.string.INVALID_USER_PASS));
                    ConfirmationDialogFragment dialogFragment =
                            ConfirmationDialogFragment.newInstance(0, getString(R.string.INVALID_USER_PASS),
                                    getString(R.string.ok), true);
                    try {
                        dialogFragment.show(getSupportFragmentManager(),
                                getScreenTag() + "#InvalidUserPassDialog");
                    } catch (IllegalStateException ex) {
                        Crashlytics.logException(ex);
                    }
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
        public void onFailure(int httpErrorCode, String msg) {
            logFailureEvents(msg);
            super.onFailure(httpErrorCode, msg);
        }

        @Override
        public void onFailure(Call<ApiResponse<LoginApiResponse>> call, Throwable t) {
            if (call != null && !call.isCanceled()) {
                logFailureEvents("Network Error");
            }
            super.onFailure(call, t);
        }

        private void logFailureEvents(String err) {
            switch (loginType) {
                case SocialAccountType.FB:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, err);
                    break;
                case SocialAccountType.GP:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, err);
                    break;
                case Constants.SIGN_IN_ACCOUNT_TYPE:
                    logSignInFailureEvent(TrackEventkeys.LOGIN_TYPE_NORMAL, err);
                    break;
                case Constants.REGISTER_ACCOUNT_TYPE:
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.FAILURE_REASON, err);
                    trackEvent(TrackingAware.REGISTRATION_FAILED, map);
                    break;
                default:
                    throw new AssertionError("Login or register type error while success(status=ERROR)");
            }
        }

        @Override
        public boolean updateProgress() {
            try {
                hideProgressDialog();
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
