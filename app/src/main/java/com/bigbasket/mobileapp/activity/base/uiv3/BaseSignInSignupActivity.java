package com.bigbasket.mobileapp.activity.base.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginConfirmActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.interfaces.EmailAddressAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.task.uiv3.LoadEmailAddressTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public abstract class BaseSignInSignupActivity extends BackButtonActivity implements EmailAddressAware {

    protected AutoCompleteTextView mEmailView;

    protected void populateAutoComplete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, new LoadEmailAddressTask<>(getCurrentActivity()).new ContactsLoader());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // Use AccountManager (API 8+)
            new LoadEmailAddressTask<>(getCurrentActivity()).new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    @Override
    public void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getCurrentActivity(), layout, emailAddressCollection);

        mEmailView.setAdapter(adapter);
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
            showProgress(false);
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
                            logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, loginApiResponse.message);
                            break;
                        case SocialAccount.GP:
                            logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, loginApiResponse.message);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, loginApiResponse.message);
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
            showProgress(false);
            handler.handleRetrofitError(error);
            switch (loginType) {
                case SocialAccount.FB:
                    logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_FACEBOOK, error.toString());
                    break;
                case SocialAccount.GP:
                    logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_GOOGLE, error.toString());
                    break;
                case Constants.SIGN_IN_ACCOUNT_TYPE:
                    logSingInFailureEvent(TrackEventkeys.LOGIN_TYPE_NORMAL, error.toString());
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


    private void logSingInFailureEvent(String type, String reason){
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.FAILURE_REASON, reason);
        map.put(TrackEventkeys.TYPE, type);
        trackEvent(TrackingAware.LOGIN_FAILED, map);
    }

    /*
    private void trackEventNewAccount(String eventKetName, String email, boolean isNewAccount) {
        HashMap<String, String> map = new HashMap<>();
        if (isNewAccount) {
            map.put(TrackEventkeys.NEW_ACCOUNT, "Yes");
        } else {
            map.put(TrackEventkeys.NEW_ACCOUNT, "No");
            map.put(TrackEventkeys.EXISTING_ACCOUNT_EMAIL, email);
        }
        trackEvent(TrackingAware.MY_ACCOUNT_FACEBOOK_LOGIN_SUCCESS, map);
        trackEvent(eventKetName, null);
    }
    */

    public abstract void showProgress(boolean show);

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

    public void togglePasswordView(EditText passwordEditText, boolean isChecked) {
        if (!isChecked) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            logShowPasswordEnabled(TrackEventkeys.YES, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            logShowPasswordEnabled(TrackEventkeys.NO, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        }
    }

    private void logShowPasswordEnabled(String enabled, String navigationCtx){
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, enabled);
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        trackEvent(TrackingAware.SHOW_PASSWORD_ENABLED, eventAttribs);
    }

    public void setTermsAndCondition(TextView txtVw) {
        txtVw.setTypeface(faceRobotoRegular);
        SpannableString spannableString = new SpannableString(txtVw.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtVw.setText(spannableString);
        txtVw.setClickable(true);
        txtVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent flatPageWebviewActivity = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_URL, MobileApiUrl.DOMAIN + "terms-and-conditions/");
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.termsAndCondHeading));
                startActivityForResult(flatPageWebviewActivity, NavigationCodes.GO_TO_HOME);
            }
        });
    }
}
