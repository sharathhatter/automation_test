package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.Gson;

public class SocialLoginConfirmActivity extends BaseSignInSignupActivity {

    private String mLoginType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_social_login_confirm, null);

        mEmailView = (AutoCompleteTextView) base.findViewById(R.id.emailInput);

        contentView.addView(base);

        SocialAccount socialAccount = getIntent().getParcelableExtra(Constants.SOCIAL_LOGIN_PARAMS);
        mLoginType = getIntent().getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
        setSocialConfirmationView(base, socialAccount);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onAccountNotLinked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSocialConfirmationView(View base, final SocialAccount socialAccount) {
        String socialServiceName = null;
        switch (mLoginType) {
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
                    onLinkToExistingAccount(email, password, socialAccount);
                }
            }
        });

        btnCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAccount(socialAccount);
            }
        });
    }

    public void onLinkToExistingAccount(String email, String password, SocialAccount socialAccount) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgress(true);
        bigBasketApiService.socialLinkAccount(email, password, mLoginType,
                new Gson().toJson(socialAccount, SocialAccount.class),
                new LoginApiResponseCallback(email, password, false, mLoginType, socialAccount));
    }

    public void onCreateNewAccount(SocialAccount socialAccount) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgress(true);
        bigBasketApiService.socialRegisterMember(mLoginType, new Gson().toJson(socialAccount, SocialAccount.class),
                new LoginApiResponseCallback(socialAccount.getEmail(), null, false, mLoginType, socialAccount));
    }

    @Override
    public void showProgress(boolean show) {
        if (show) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onBackPressed() {
        onAccountNotLinked();
    }

    private void onAccountNotLinked() {
        Intent data = new Intent();
        data.putExtra(Constants.SOCIAL_LOGIN_TYPE, mLoginType);
        setResult(Constants.SOCIAL_ACCOUNT_NOT_LINKED, data);
        finish();
    }
}
