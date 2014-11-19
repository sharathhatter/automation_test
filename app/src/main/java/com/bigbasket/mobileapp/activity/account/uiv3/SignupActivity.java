package com.bigbasket.mobileapp.activity.account.uiv3;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BaseSignInSignupActivity;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class SignupActivity extends BaseSignInSignupActivity {

    // UI References
    private View mSignUpForm;
    private ProgressBar mSignUpProgressBar;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mMobileNumView;
    private CheckBox mChkAcceptTerms;
    private CheckBox mChkReceivePromos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = getLayoutInflater();

        View base = inflater.inflate(R.layout.uiv3_signup, null);
        setTitle(getString(R.string.signUp));
        contentView.addView(base);
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        String currentCityName = prefer.getString(Constants.CITY, "");

        mSignUpForm = base.findViewById(R.id.signup_form);
        mSignUpProgressBar = (ProgressBar) base.findViewById(R.id.signup_progress);
        mPasswordView = (EditText) base.findViewById(R.id.editTextPasswd);
        mFirstNameView = (EditText) base.findViewById(R.id.editTextFirstName);
        mLastNameView = (EditText) base.findViewById(R.id.editTextLastName);
        mMobileNumView = (EditText) base.findViewById(R.id.editTextMobileNumber);
        mChkAcceptTerms = (CheckBox) base.findViewById(R.id.chkAcceptTerms);
        mChkReceivePromos = (CheckBox) base.findViewById(R.id.chkReceivePromos);
        mEmailView = (AutoCompleteTextView) base.findViewById(R.id.editTextEmailSignup);

        ((EditText) base.findViewById(R.id.editTextCity)).setText(currentCityName);
        populateAutoComplete();
    }

    public void OnRegisterButtonClicked(View v) {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mMobileNumView.setError(null);

        boolean cancel = false;
        View focusView = null;


        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String mobileNumber = mMobileNumView.getText().toString();

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
        } else if (password.length() < 4) {
            reportFormInputFieldError(mPasswordView, getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for first name
        if (TextUtils.isEmpty(firstName)) {
            reportFormInputFieldError(mFirstNameView, "Please provide your first-name");
            focusView = mFirstNameView;
            cancel = true;
        }

        // Check for last name
        if (TextUtils.isEmpty(lastName)) {
            reportFormInputFieldError(mLastNameView, "Please provide your last-name");
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for mobile number
        if (TextUtils.isEmpty(mobileNumber)) {
            reportFormInputFieldError(mMobileNumView, "Please provide your mobile-number");
            focusView = mMobileNumView;
            cancel = true;
        }
        if (!TextUtils.isDigitsOnly(mobileNumber)) {
            reportFormInputFieldError(mMobileNumView, "Mobile number can only have digits");
            focusView = mMobileNumView;
            cancel = true;
        }
        if (mobileNumber.length() > 10) {
            reportFormInputFieldError(mMobileNumView, "Mobile number can only have 10 digits");
            focusView = mMobileNumView;
            cancel = true;
        }

        // Check is user agreed to terms & conditions
        if (!mChkAcceptTerms.isChecked()) {
            showToast(getString(R.string.acceptTermsMsg));
            focusView = mChkAcceptTerms;
            cancel = true;
        }

        if (cancel) {
            // There was an error, don't sign-up
            focusView.requestFocus();
        } else {
            showProgress(true);
            startMemberRegistration(email, password, firstName, lastName, mobileNumber);
        }
    }

    private void startMemberRegistration(String email, String passwd, String firstName,
                                         String lastName, String mobileNumber) {
        JsonObject userDetailsJsonObj = new JsonObject();
        userDetailsJsonObj.addProperty(Constants.EMAIL, email);
        userDetailsJsonObj.addProperty(Constants.FIRSTNAME, firstName);
        userDetailsJsonObj.addProperty(Constants.LASTNAME, lastName);
        userDetailsJsonObj.addProperty(Constants.PASSWORD, passwd);
        userDetailsJsonObj.addProperty(Constants.MOBILE_NUMBER, mobileNumber);
        userDetailsJsonObj.addProperty(Constants.NEWSLETTER_SUBSCRIPTION, mChkReceivePromos.isChecked());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String cityId = preferences.getString(Constants.CITY_ID, "");
        userDetailsJsonObj.addProperty(Constants.CITY_ID, cityId);

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.USER_DETAILS, userDetailsJsonObj.toString());
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.REGISTER_MEMBER, params, true,
                AuthParameters.getInstance(this), new BasicCookieStore(), null, true);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.REGISTER_MEMBER)) {
            showProgress(false);
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();

                    String bbToken = responseJsonObj.get(Constants.BB_TOKEN).getAsString();
                    String mid = responseJsonObj.get(Constants.MID_KEY).getAsString();
                    String firstName = mFirstNameView.getText().toString();
                    String lastName = mLastNameView.getText().toString();
                    String email = mEmailView.getText().toString();
                    editor.putString(Constants.BBTOKEN_KEY, bbToken);
                    editor.putString(Constants.MID_KEY, mid);
                    editor.putString(Constants.FIRST_NAME_PREF, firstName);
                    editor.putString(Constants.MEMBER_FULL_NAME_KEY, firstName + " " + lastName);
                    editor.putString(Constants.MEMBER_EMAIL_KEY, email);
                    editor.commit();
                    OnRegistrationSuccess(email, mPasswordView.getText().toString());
                    break;
                case Constants.ERROR:
                    showAlertDialog(this, null, getString(R.string.server_error));
                    // TODO : Replace with error
                    break;
            }
        } else if (httpOperationResult.getUrl().contains(Constants.LOGIN)) {
            showProgress(false);
            onLoginSuccess();
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void OnRegistrationSuccess(String email, String password) {
        showProgress(true);
        AuthParameters.updateInstance(this);
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, password);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.LOGIN, params, true,
                AuthParameters.getInstance(this), new BasicCookieStore());
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mSignUpProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mSignUpProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignUpProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mSignUpProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}