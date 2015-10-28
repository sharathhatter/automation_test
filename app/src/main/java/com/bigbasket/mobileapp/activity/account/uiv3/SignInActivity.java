package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
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
import com.bigbasket.mobileapp.handler.OnCompoundDrawableClickListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
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
    private EditText mEmailView;
    private boolean mIsPasswordVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_LOGIN_SCREEN);

        setTitle(getString(R.string.signInCapsVerb));

        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btnFBLogin));

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.emailInput);

        ((TextView) findViewById(R.id.txtOrSeparator)).setTypeface(faceRobotoRegular);
        ((TextView) findViewById(R.id.lblConnectUsing)).setTypeface(faceRobotoLight);
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
        mPasswordView.setOnTouchListener(new OnCompoundDrawableClickListener(OnCompoundDrawableClickListener.DRAWABLE_RIGHT) {
            @Override
            public void onRightDrawableClicked() {
                mIsPasswordVisible = !mIsPasswordVisible;
                togglePasswordView(mPasswordView, mIsPasswordVisible);
            }

            @Override
            public void onLeftDrawableClicked() {

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
        txtForgotPasswd.setTypeface(faceRobotoLight);
        txtForgotPasswd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trackEvent(TrackingAware.FORGOT_PASSWORD_CLICKED, null);
                showForgotPasswordDialog();
            }
        });

        TextView lblSignUp = (TextView) findViewById(R.id.lblSignUp);
        lblSignUp.setTypeface(faceRobotoLight);
        SpannableString signUpSpannable = new SpannableString(lblSignUp.getText());
        signUpSpannable.setSpan(new UnderlineSpan(), 0, signUpSpannable.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        lblSignUp.setText(signUpSpannable);
        lblSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRegistrationPage();
            }
        });

        mChkRememberMe = (CheckBox) findViewById(R.id.chkRememberMe);
        mChkRememberMe.setTypeface(faceRobotoLight);

        initializeRememberedDataForLoginInput();

        mChkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logRememberMeEnabled(isChecked ? TrackEventkeys.YES : TrackEventkeys.NO);
            }
        });
        trackEvent(TrackingAware.LOGIN_SHOWN, null);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_login;
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mPasswordView != null) {
            hideKeyboard(this, mPasswordView);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.

        TextInputLayout textInputEmail = (TextInputLayout) findViewById(R.id.textInputEmail);
        TextInputLayout textInputPasswd = (TextInputLayout) findViewById(R.id.textInputPasswd);
        UIUtil.resetFormInputField(textInputEmail);
        UIUtil.resetFormInputField(textInputPasswd);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            UIUtil.reportFormInputFieldError(textInputPasswd, "Please enter your password");
            focusView = textInputEmail;
            cancel = true;
        }

        // Check for a valid password length
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            UIUtil.reportFormInputFieldError(textInputPasswd,
                    getString(R.string.psswordMst6Digit));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            UIUtil.reportFormInputFieldError(textInputEmail, "Please enter your e-mail address");
            focusView = mEmailView;
            cancel = true;
        } else if (!UIUtil.isValidEmail(email)) {
            UIUtil.reportFormInputFieldError(textInputEmail, getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
                new LoginApiResponseCallback(email, password, mChkRememberMe.isChecked(),
                        Constants.SIGN_IN_ACCOUNT_TYPE, null));
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.FORGOT_PASSWORD_DIALOG:
                    showForgotPasswordDialog();
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    private void showForgotPasswordDialog() {
        new InputDialog<SignInActivity>(this, R.string.emailNewPassword, R.string.cancel,
                R.string.forgotPasswd, R.string.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
            @Override
            public void onPositiveButtonClicked(String inputEmail) {
                if (TextUtils.isEmpty(inputEmail)) {
                    showAlertDialog(
                            getResources().getString(R.string.error), getResources().getString(R.string.emailBlank),
                            DialogButton.OK, DialogButton.CANCEL, Constants.FORGOT_PASSWORD_DIALOG);
                    return;
                }
                if (!UIUtil.isValidEmail(inputEmail)) {
                    showAlertDialog(
                            getResources().getString(R.string.error), getResources().getString(R.string.error_invalid_email),
                            DialogButton.OK, DialogButton.CANCEL, Constants.FORGOT_PASSWORD_DIALOG);
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



