package com.bigbasket.mobileapp.activity.account.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BaseSignInSignupActivity;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class SocialLoginConfirmActivity extends BaseSignInSignupActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_social_login_confirm, null);

        mEmailView = (AutoCompleteTextView) base.findViewById(R.id.emailInput);

        contentView.addView(base);

        SocialAccount socialAccount = getIntent().getParcelableExtra(Constants.SOCIAL_LOGIN_PARAMS);
        String loginType = getIntent().getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
        setSocialConfirmationView(base, loginType, socialAccount);
    }

    public void setSocialConfirmationView(View base,
                                          final String loginType, final SocialAccount socialAccount) {
        String socialServiceName = null;
        switch (loginType) {
            case SocialAccount.GP:
                socialServiceName = "Google";
                break;
            case SocialAccount.FB:
                socialServiceName = "Facebook";
                break;
        }

        if (socialServiceName == null) return;
        String memberName = socialAccount.getDisplayName();
        String memberEmail = socialAccount.getEmail();

        TextView txtSocialLoggedInAs = (TextView) base.findViewById(R.id.txtSocialLoggedInAs);
        txtSocialLoggedInAs.setTypeface(faceRobotoRegular);
        TextView txtNoAccountForSocialLogin = (TextView) base.findViewById(R.id.txtNoAccountForSocialLogin);
        txtNoAccountForSocialLogin.setTypeface(faceRobotoRegular);
        TextView txtSocialChooseMethod = (TextView) base.findViewById(R.id.txtSocialChooseMethod);
        txtSocialChooseMethod.setTypeface(faceRobotoRegular);
        RadioButton rbtnLinkToExistingSocialAccount = (RadioButton)
                base.findViewById(R.id.rbtnLinkToExistingSocialAccount);
        rbtnLinkToExistingSocialAccount.setTypeface(faceRobotoRegular);
        RadioButton rbtnCreateAccount = (RadioButton) base.findViewById(R.id.rbtnCreateAccount);
        rbtnCreateAccount.setTypeface(faceRobotoRegular);
        final CheckBox chkAcceptTerms = (CheckBox) base.findViewById(R.id.chkAcceptTerms);
        chkAcceptTerms.setTypeface(faceRobotoRegular);
        mEmailView = (AutoCompleteTextView) base.findViewById(R.id.emailInput);
        populateAutoComplete();
        final EditText editTextPasswd = (EditText) base.findViewById(R.id.editTextPasswd);
        final Button btnLinkToExistingSocialAccount = (Button) base.findViewById(R.id.btnLinkToExistingSocialAccount);
        final Button btnCreateNewAccount = (Button) base.findViewById(R.id.btnCreateNewAccount);
        final TextView txtCreateNewAccount = (TextView) base.findViewById(R.id.txtCreateNewAccount);
        txtCreateNewAccount.setTypeface(faceRobotoRegular);

        txtSocialLoggedInAs.setText("You are logged in to " + socialServiceName + " as " +
                memberName + " (" + memberEmail + ")");
        txtNoAccountForSocialLogin.setText("We do not have a BigBasket account with " +
                memberEmail);
        txtSocialChooseMethod.setText("To complete the " + socialServiceName + " account linking, "
                + "select from the two options mentioned below");
        txtSocialChooseMethod.setTypeface(null, Typeface.BOLD);
        rbtnLinkToExistingSocialAccount.setText("Link " + memberName + " " + socialServiceName + " account " +
                "with my existing BigBasket account mentioned below");
        rbtnCreateAccount.setText("I do not have a BigBasket account");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String currentCityName = preferences.getString(Constants.CITY, "");
        txtCreateNewAccount.setText("Create a new BigBasket account for " + currentCityName +
                " city and link it with my " + socialServiceName + " account");

        final View layoutLoginInput = base.findViewById(R.id.layoutLoginInput);
        rbtnLinkToExistingSocialAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutLoginInput.setVisibility(View.VISIBLE);
                    txtCreateNewAccount.setVisibility(View.GONE);
                    btnCreateNewAccount.setVisibility(View.GONE);
                    btnLinkToExistingSocialAccount.setVisibility(View.VISIBLE);
                    chkAcceptTerms.setVisibility(View.VISIBLE);
                }
            }
        });

        rbtnCreateAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutLoginInput.setVisibility(View.GONE);
                    txtCreateNewAccount.setVisibility(View.VISIBLE);
                    btnCreateNewAccount.setVisibility(View.VISIBLE);
                    btnLinkToExistingSocialAccount.setVisibility(View.GONE);
                    chkAcceptTerms.setVisibility(View.GONE);
                }
            }
        });

        btnLinkToExistingSocialAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailView.setError(null);
                editTextPasswd.setError(null);
                // Store values at the time of the login attempt.
                String email = mEmailView.getText().toString().trim();
                String password = editTextPasswd.getText().toString().trim();

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
                    reportFormInputFieldError(editTextPasswd, "Please enter your password");
                    focusView = editTextPasswd;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    onLinkToExistingAccount(email, password, loginType, socialAccount);
                }
            }
        });

        btnCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAccount(loginType, socialAccount);
            }
        });
    }

    public void onLinkToExistingAccount(String email, String password, String loginType,
                                        SocialAccount socialAccount) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, password);
        params.put(Constants.SOCIAL_LOGIN_TYPE, loginType);
        params.put(Constants.SOCIAL_LOGIN_PARAMS,
                new Gson().toJson(socialAccount, SocialAccount.class));

        HashMap<Object, String> loginTypeMap = new HashMap<>();
        loginTypeMap.put(Constants.SOCIAL_LOGIN_TYPE, loginType);
        loginTypeMap.put(Constants.EMAIL, email);

        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.SOCIAL_LINK_ACCOUNT_URL,
                params, true, AuthParameters.getInstance(this), new BasicCookieStore(),
                loginTypeMap);
    }

    public void onCreateNewAccount(String loginType, SocialAccount socialAccount) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SOCIAL_LOGIN_TYPE, loginType);
        params.put(Constants.SOCIAL_LOGIN_PARAMS,
                new Gson().toJson(socialAccount, SocialAccount.class));

        HashMap<Object, String> loginTypeMap = new HashMap<>();
        loginTypeMap.put(Constants.SOCIAL_LOGIN_TYPE, loginType);
        loginTypeMap.put(Constants.EMAIL, socialAccount.getEmail());

        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.SOCIAL_REGISTER_MEMBER_URL,
                params, true, AuthParameters.getInstance(this), new BasicCookieStore(),
                loginTypeMap);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.SOCIAL_LINK_ACCOUNT_URL) ||
                url.contains(Constants.SOCIAL_REGISTER_MEMBER_URL)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    String loginType = httpOperationResult.getAdditionalCtx() != null ?
                            httpOperationResult.getAdditionalCtx().get(Constants.SOCIAL_LOGIN_TYPE) : null;
                    String email = httpOperationResult.getAdditionalCtx() != null ?
                            httpOperationResult.getAdditionalCtx().get(Constants.EMAIL) : null;
                    saveLoginUserDetailInPreference(responseJsonObj, loginType, email, null, false);
                    break;
                case Constants.ERROR:
                    //TODO : Replace with handler
                    String errorType = responseJsonObj.get(Constants.ERROR_TYPE).getAsString();
                    switch (errorType) {
                        case Constants.INVALID_USER_PASS:
                            showAlertDialog(this, null, getString(R.string.INVALID_USER_PASS));
                            break;
                        default:
                            showAlertDialog(this, null, getString(R.string.server_error));
                            break;
                    }
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }
}
