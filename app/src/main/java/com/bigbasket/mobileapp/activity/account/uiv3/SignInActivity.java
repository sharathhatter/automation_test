package com.bigbasket.mobileapp.activity.account.uiv3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
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

public class SignInActivity extends FacebookAndGPlusSigninBaseActivity {

    // UI references.
    private EditText mPasswordView;
    private View mProgressView;
    private Button mPlusSignInButton;
    private View mLoginFormView;
    private View mBaseView;
    private LoginButton mFacebookLoginButton;
    private CheckBox mChkRememberMe;

    private SocialAccount mSocialAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = getLayoutInflater();
        mBaseView = inflater.inflate(R.layout.uiv3_login, null);
        contentView.addView(mBaseView);

        setTitle(getString(R.string.signIn));
        // Find the Google+ sign in button.
        mPlusSignInButton = (Button) mBaseView.findViewById(R.id.plus_sign_in_button);
        if (supportsGooglePlayServices()) {
            // Set a listener to connect the user when the G+ button is clicked.
            mPlusSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInViaGPlus();
                }
            });
        } else {
            // Don't offer G+ sign in if the app's version is too low to support Google Play
            // Services.
            mPlusSignInButton.setVisibility(View.GONE);
            Button signOutButton = (Button) mBaseView.findViewById(R.id.plus_sign_out_button);
            signOutButton.setVisibility(View.GONE);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) mBaseView.findViewById(R.id.emailInput);
        populateAutoComplete();

        mPasswordView = (EditText) mBaseView.findViewById(R.id.editTextPasswd);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) mBaseView.findViewById(R.id.btnLogin);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = mBaseView.findViewById(R.id.login_form);
        mProgressView = mBaseView.findViewById(R.id.login_progress);

        TextView txtSignup = (TextView) mBaseView.findViewById(R.id.txtSignup);
        SpannableString spannableString = new SpannableString(getString(R.string.loginPageSignUpText));
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtSignup.setText(spannableString);

        mFacebookLoginButton = (LoginButton) mBaseView.findViewById(R.id.btnFBLogin);

        mChkRememberMe = (CheckBox) mBaseView.findViewById(R.id.chkRememberMe);

        if (isInLogoutMode()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String socialAccountType = preferences.getString(Constants.SOCIAL_ACCOUNT_TYPE, "");
            boolean isFbLoggedIn = socialAccountType.equalsIgnoreCase(SocialAccount.FB);
            boolean isGplusLoggedIn = socialAccountType.equalsIgnoreCase(SocialAccount.GP);
            if (isFbLoggedIn) {
                initializeFacebookLogin(mFacebookLoginButton);
            } else if (isGplusLoggedIn) {
                initializeGooglePlusSignIn();
                showProgressDialog(getString(R.string.please_wait));
            }
            updateViewStateInLogoutMode();
            setTitle(getString(R.string.signOut));
        } else {
            initializeRememberedDataForLoginInput();
            initializeGooglePlusSignIn();
            initializeFacebookLogin(mFacebookLoginButton);
        }

        CheckBox chkShowPassword = (CheckBox) mBaseView.findViewById(R.id.chkShowPasswd);
        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                togglePasswordView(mPasswordView, isChecked);
            }
        });
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

    private void updateViewStateInLogoutMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String socialAccountType = preferences.getString(Constants.SOCIAL_ACCOUNT_TYPE, "");
        boolean isFbLoggedIn = socialAccountType.equalsIgnoreCase(SocialAccount.FB);
        boolean isGplusLoggedIn = socialAccountType.equalsIgnoreCase(SocialAccount.GP);
        View layoutEmailLogin = mBaseView.findViewById(R.id.layoutEmailLogin);
        if (isGplusLoggedIn || isFbLoggedIn) {
            layoutEmailLogin.setVisibility(View.GONE);
        } else {
            layoutEmailLogin.setVisibility(View.VISIBLE);
        }

        if (isGplusLoggedIn) {
            mFacebookLoginButton.setVisibility(View.GONE);
        }
        if (isFbLoggedIn) {
            mFacebookLoginButton.setVisibility(View.VISIBLE);
            mPlusSignInButton.setVisibility(View.GONE);
            Button signOutButton = (Button) mBaseView.findViewById(R.id.plus_sign_out_button);
            signOutButton.setVisibility(View.GONE);
        }
    }

    public boolean isInLogoutMode() {
        return getIntent().getBooleanExtra(Constants.SOCIAL_LOGOUT, false);
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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            startLogin(email, password);
        }
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

            if (mLoginFormView != null) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            }

            if (mProgressView != null) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            }
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.

            if (mProgressView != null) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (mLoginFormView != null) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void onPlusLogoutButtonClicked(View v) {
        signOutFromGplus();
    }

    @Override
    protected void onPlusClientSignIn(String email, Person person) {

        //Set up sign out and disconnect buttons.
        Button signOutButton = (Button) mBaseView.findViewById(R.id.plus_sign_out_button);
        signOutButton.setVisibility(View.VISIBLE);
        signOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutFromGplus();
            }
        });

        if (isInLogoutMode()) {
            hideProgressDialog();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            signOutFromGplus();
            showAlertDialog(null, "Unable to get your email-address\n" +
                    "Please check your privacy settings.");
            return;
        }
        if (person == null) {
            signOutFromGplus();
            showAlertDialog(null, "Unable to read your profile information\n" +
                    "Please check your privacy settings.");
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

        mSocialAccount = new SocialAccount(email, displayName, gender, profileLink, uid,
                isVerified, firstName, lastName, imgUrl);

        startSocialLogin(SocialAccount.GP);
    }

    private void startSocialLogin(String loginType) {
        showProgress(true);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Gson gson = new Gson();
        bigBasketApiService.socialLogin(loginType, gson.toJson(mSocialAccount, SocialAccount.class),
                new LoginApiResponseCallback(mEmailView.getText().toString().trim(),
                        mPasswordView.getText().toString().trim(),
                        mChkRememberMe.isChecked(), loginType, mSocialAccount));
    }

    @Override
    public void onFacebookSignIn(Session facebookSession) {
        showProgress(true);
        Request facebookUserDetailRequest = Request.newMeRequest(facebookSession, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                showProgress(false);
                if (user != null) {
                    String email = response.getGraphObject().getProperty(Constants.EMAIL).toString();
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
                    mSocialAccount = new SocialAccount(email, firstName, gender, profileLink, uid,
                            isVerified, firstName, lastName, imgUrl);
                    startSocialLogin(SocialAccount.FB);
                } else if (response.getError() != null) {
                    showAlertDialog(null, response.getError().getErrorMessage());
                }
            }
        });
        Request.executeBatchAsync(facebookUserDetailRequest);
    }

    @Override
    public void onFacebookSignOut() {
        doLogout();
        setResult(NavigationCodes.GO_TO_HOME);
        finish();
    }

    @Override
    protected void onPlusClientBlockingUI(boolean show) {
        showProgress(show);
    }

    @Override
    protected void updatePlusConnectedButtonState() {
        if (getPlusClient() == null) return;

        boolean connected = getPlusClient().isConnected();

        if (mPlusSignInButton != null) {
            mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
        }
        if (mLoginFormView != null) {
            mLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onPlusClientRevokeAccess() {
        // any stored user data here.
        showProgress(false);

        Button signOutButton = (Button) mBaseView.findViewById(R.id.plus_sign_out_button);
        signOutButton.setVisibility(View.GONE);

        mPlusSignInButton.setVisibility(View.VISIBLE);
        initializeGooglePlusSignIn();
    }

    @Override
    protected void onPlusClientSignOut() {
        Button signOutButton = (Button) mBaseView.findViewById(R.id.plus_sign_out_button);
        signOutButton.setVisibility(View.GONE);

        doLogout();
        setResult(NavigationCodes.GO_TO_HOME);
        finish();
    }

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    private boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private void startLogin(String email, String password) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        bigBasketApiService.login(email, password,
                new LoginApiResponseCallback(email, password, mChkRememberMe.isChecked()));
    }

    public void OnRegistrationLinkClicked(View v) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.SOCIAL_ACCOUNT_NOT_LINKED && data != null) {
            String loginType = data.getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
            if (!TextUtils.isEmpty(loginType)) {
                switch (loginType) {
                    case SocialAccount.GP:
                        revokeGPlusAccess();
                        break;
                    case SocialAccount.FB:
                        revokeFbAccess();
                        initializeFacebookLogin(mFacebookLoginButton);
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void revokeGPlusAccess() {
        showProgress(true);
        super.revokeGPlusAccess();
    }
}



