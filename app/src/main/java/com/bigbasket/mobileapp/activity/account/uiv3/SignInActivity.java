package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
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
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.click.OnCompoundDrawableClickListener;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import retrofit.Call;

public class SignInActivity extends SocialLoginActivity {

    // UI references.
    private EditText mPasswordView;
    private CheckBox mChkRememberMe;
    private EditText mEmailView;
    private boolean mIsPasswordVisible;
    private TextInputLayout mTextInputEmail, mTextInputPasswd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setCurrentScreenName(TrackEventkeys.NC_LOGIN_SCREEN);

        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btn_fb_login));

        mTextInputEmail = (TextInputLayout) findViewById(R.id.text_input_email);
        mTextInputPasswd = (TextInputLayout) findViewById(R.id.text_input_passwd);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email_input);

        ((TextView) findViewById(R.id.txt_or_separator)).setTypeface(faceRobotoRegular);
        ((TextView) findViewById(R.id.lbl_connect_using)).setTypeface(faceRobotoLight);
        mPasswordView = (EditText) findViewById(R.id.edit_text_passwd);
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

        Button mEmailSignInButton = (Button) findViewById(R.id.btn_login);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logSignInBtnClickEvent(TrackEventkeys.LOGIN_TYPE_NORMAL);
                attemptLogin();
            }
        });
        mEmailSignInButton.setTypeface(faceRobotoRegular);

        TextView txtForgotPasswd = (TextView) findViewById(R.id.txt_forgot_passwd);
        txtForgotPasswd.setTypeface(faceRobotoLight);
        txtForgotPasswd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onForgotPasswordClick();
            }
        });

        TextView lblSignUp = (TextView) findViewById(R.id.lbl_sign_up);
        lblSignUp.setTypeface(faceRobotoLight);
        lblSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.resetFormInputField(mTextInputEmail);
                UIUtil.resetFormInputField(mTextInputPasswd);
                launchRegistrationPage();
            }
        });

        TextView txtTermsAndCond = (TextView) findViewById(R.id.txt_terms_and_conditions);

        ClickableSpan termsAndCondClickSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                launchFlatPage(Constants.TERMS_AND_COND_URL);
            }
        };

        final ClickableSpan privacyPolicyClickSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                launchFlatPage(Constants.PRIVACY_POLICY_URL);
            }
        };

        final UnderlineSpan underLineSpan = new UnderlineSpan();
        final ForegroundColorSpan whiteForegroundSpan =
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white_60p));

        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(getString(R.string.terms_and_cond_start));
        int start = spannableBuilder.length();
        spannableBuilder.append(getString(R.string.terms_and_cond));
        int end = spannableBuilder.length();
        spannableBuilder.setSpan(termsAndCondClickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(whiteForegroundSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(underLineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableBuilder.append(getString(R.string.and));

        start = spannableBuilder.length();
        spannableBuilder.append(getString(R.string.privacy_policy));
        end = spannableBuilder.length();
        spannableBuilder.setSpan(privacyPolicyClickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(whiteForegroundSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(underLineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtTermsAndCond.setHighlightColor(Color.TRANSPARENT);
        txtTermsAndCond.setText(spannableBuilder);
        txtTermsAndCond.setMovementMethod(LinkMovementMethod.getInstance());

        mChkRememberMe = (CheckBox) findViewById(R.id.chk_remember_me);
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


    private void launchFlatPage(String url) {
        try {
            Intent intent = new Intent(SignInActivity.this, BackButtonActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
            intent.putExtra(Constants.WEBVIEW_URL, URLDecoder.decode(url, "UTF-8"));
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } catch (UnsupportedEncodingException e) {
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onSignInCancelled();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onSignInCancelled();
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    private void onSignInCancelled() {
        setResult(NavigationCodes.SIGN_IN_CANCELLED);
        finish();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.

        UIUtil.resetFormInputField(mTextInputEmail);
        UIUtil.resetFormInputField(mTextInputPasswd);

        hideKeyboard(getCurrentActivity(), mEmailView);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            UIUtil.reportFormInputFieldError(mTextInputPasswd, getString(R.string.empty_password_error));
            focusView = mTextInputEmail;
            cancel = true;
        } else if (password.length() < 6) {
            // Check for a valid password length
            UIUtil.reportFormInputFieldError(mTextInputPasswd,
                    getString(R.string.psswordMst6Digit));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            UIUtil.reportFormInputFieldError(mTextInputEmail, getString(R.string.empty_email_address_error));
            focusView = mEmailView;
            cancel = true;
        } else if (!UIUtil.isValidEmail(email)) {
            UIUtil.reportFormInputFieldError(mTextInputEmail, getString(R.string.error_invalid_email));
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
        Call<ApiResponse<LoginApiResponse>> call = bigBasketApiService.login(email, password);
        call.enqueue(new LoginApiResponseCallback(email, password, mChkRememberMe.isChecked(),
                Constants.SIGN_IN_ACCOUNT_TYPE, null));
    }

    private void onForgotPasswordClick() {
        TextInputLayout textInputEmail = (TextInputLayout) findViewById(R.id.text_input_email);
        TextInputLayout textInputPasswd = (TextInputLayout) findViewById(R.id.text_input_passwd);
        UIUtil.resetFormInputField(textInputEmail);
        UIUtil.resetFormInputField(textInputPasswd);

        String email = mEmailView.getText().toString().trim();
        boolean isValid = true;
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            UIUtil.reportFormInputFieldError(textInputEmail, getString(R.string.empty_email_address_error));
            isValid = false;
        } else if (!UIUtil.isValidEmail(email)) {
            UIUtil.reportFormInputFieldError(textInputEmail, getString(R.string.error_invalid_email));
            isValid = false;
        }
        if (!isValid) {
            mEmailView.requestFocus();
            return;
        }

        hideKeyboard(getCurrentActivity(), mEmailView);
        requestNewPassword(email);
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        trackEvent(TrackingAware.FORGOT_PASSWORD_DIALOG_SHOWN, eventAttribs);
        LocalyticsWrapper.tagScreen(TrackEventkeys.FORGOT_PASSWORD_SCREEN);
    }

    private void requestNewPassword(String email) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<OldBaseApiResponse> call = bigBasketApiService.forgotPassword(email);
        call.enqueue(new BBNetworkCallback<OldBaseApiResponse>(this) {
            @Override
            public void onSuccess(OldBaseApiResponse forgotPasswordApiResponse) {
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
            public void onFailure(int httpErrorCode, String msg) {
                super.onFailure(httpErrorCode, msg);
                logForgotPasswordFailure(msg);
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



