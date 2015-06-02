package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.handler.OnRightCompoundDrawableClicked;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.InputDialog;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public class SignInActivity extends BackButtonActivity {

    // UI references.
    private EditText mPasswordView;
    private CheckBox mChkRememberMe;
    private AutoCompleteTextView mEmailView;
    private boolean mIsPasswordVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.signInCaps));

        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btnFBLogin));

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.emailInput);
        populateAutoComplete(mEmailView);

        mPasswordView = (EditText) findViewById(R.id.editTextPasswd);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    hideKeyboard(getCurrentActivity(), mPasswordView);
                }
                return false;
            }
        });
        mPasswordView.setOnTouchListener(new OnRightCompoundDrawableClicked() {
            @Override
            public void onRightDrawableClicked() {
                mIsPasswordVisible = !mIsPasswordVisible;
                togglePasswordView(mPasswordView, mIsPasswordVisible);
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.btnLogin);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logSignInBtnClickEvent(TrackEventkeys.LOGIN_TYPE_NORMAL);
                attemptLogin();
            }
        });
        mEmailSignInButton.setTypeface(faceRobotoRegular);

        TextView txtForgotPasswd = (TextView) findViewById(R.id.txtForgotPasswd);
        txtForgotPasswd.setTypeface(faceRobotoRegular);
        txtForgotPasswd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trackEvent(TrackingAware.FORGOT_PASSWORD_CLICKED, null);
                showForgotPasswordDialog();
            }
        });

        mChkRememberMe = (CheckBox) findViewById(R.id.chkRememberMe);
        mChkRememberMe.setTypeface(faceRobotoRegular);

        initializeRememberedDataForLoginInput();

        mChkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logRememberMeEnabled(isChecked ? TrackEventkeys.YES : TrackEventkeys.NO);
            }
        });

        if (!TextUtils.isEmpty(getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX))) {
            Map<String, String> eventAttribs = new HashMap<>();
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
            trackEvent(TrackingAware.LOGIN_SHOWN, eventAttribs);
        }
        setTermsAndCondition((TextView) findViewById(R.id.txtSigninTermsAndCond), getString(R.string.byLoggingIn),
                getString(R.string.termsAndCondHeading), getString(R.string.authFooterSeparator), getString(R.string.privacyPolicy));
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_login;
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login_menu, menu);
    }

    private void logRememberMeEnabled(String enabled) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, enabled);
        trackEvent(TrackingAware.LOGIN_REMEMBER_ME_ENABLED, eventAttribs);
    }

    private void initializeRememberedDataForLoginInput() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean rememberMe = preferences.getBoolean(Constants.REMEMBER_ME_PREF, false);
        if (rememberMe) {
            String email = preferences.getString(Constants.EMAIL_PREF, null);
            String passwd = preferences.getString(Constants.PASSWD_PREF, null);
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(passwd)) {
                mChkRememberMe.setChecked(true);
                mEmailView.setText(email);
                mPasswordView.setText(passwd);
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            reportFormInputFieldError(mEmailView, getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!UIUtil.isValidEmail(email)) {
            reportFormInputFieldError(mEmailView, getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            reportFormInputFieldError(mPasswordView, "Please enter your password");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password length
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            reportFormInputFieldError(mPasswordView,
                    getString(R.string.psswordMst6Digit));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgressDialog(getString(R.string.please_wait));
            startLogin(email, password);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private void startLogin(String email, String password) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        bigBasketApiService.login(email, password,
                new LoginApiResponseCallback(email, password, mChkRememberMe.isChecked(), Constants.SIGN_IN_ACCOUNT_TYPE));
    }

    private void showForgotPasswordDialog() {
        new InputDialog<SignInActivity>(this, R.string.emailNewPassword, R.string.cancel,
                R.string.forgotPasswd, R.string.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
            @Override
            public void onPositiveButtonClicked(String inputEmail) {
                if (TextUtils.isEmpty(inputEmail)) {
                    showToast("Please enter an email address");
                    return;
                }
                if (!UIUtil.isValidEmail(inputEmail)) {
                    showToast(getString(R.string.error_invalid_email));
                    return;
                }
                requestNewPassword(inputEmail);
            }
        }.show();
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        trackEvent(TrackingAware.FORGOT_PASSWORD_DIALOG_SHOWN, eventAttribs);
        LocalyticsWrapper.tagScreen(TrackEventkeys.FORGOT_PASSWORD_SCREEN);
    }

    private void requestNewPassword(String email) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.forgotPassword(email, new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse forgotPasswordApiResponse, retrofit.client.Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (forgotPasswordApiResponse.status) {
                    case Constants.OK:
                        showToast(getString(R.string.newPasswordSent));
                        break;
                    default:
                        logForgotPasswordFailure(forgotPasswordApiResponse.message);
                        handler.sendEmptyMessage(forgotPasswordApiResponse.getErrorTypeAsInt(), forgotPasswordApiResponse.message);
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
                logForgotPasswordFailure(error.toString());
            }
        });
    }

    private void logForgotPasswordFailure(String reason) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.FAILURE_REASON, reason);
        trackEvent(TrackingAware.FORGOT_PASSWORD_EMAIL_CLICKED, eventAttribs);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SIGN_IN_SCREEN;
    }
}



