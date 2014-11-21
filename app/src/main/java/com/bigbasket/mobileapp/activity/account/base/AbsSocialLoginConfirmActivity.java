package com.bigbasket.mobileapp.activity.account.base;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public abstract class AbsSocialLoginConfirmActivity extends BaseSignInSignupActivity {

    private View mBaseView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBaseView = inflater.inflate(R.layout.uiv3_social_login_confirm, null);
        contentView.addView(mBaseView);
    }

    public void setSocialConfirmationView(String socialServiceName, String memberName,
                                          String memberEmail) {
        TextView txtSocialLoggedInAs = (TextView) mBaseView.findViewById(R.id.txtSocialLoggedInAs);
        txtSocialLoggedInAs.setTypeface(faceRobotoRegular);
        TextView txtNoAccountForSocialLogin = (TextView) mBaseView.findViewById(R.id.txtNoAccountForSocialLogin);
        txtNoAccountForSocialLogin.setTypeface(faceRobotoRegular);
        TextView txtSocialChooseMethod = (TextView) mBaseView.findViewById(R.id.txtSocialChooseMethod);
        txtSocialChooseMethod.setTypeface(faceRobotoRegular);
        RadioButton rbtnLinkToExistingSocialAccount = (RadioButton)
                mBaseView.findViewById(R.id.rbtnLinkToExistingSocialAccount);
        rbtnLinkToExistingSocialAccount.setTypeface(faceRobotoRegular);
        RadioButton rbtnCreateAccount = (RadioButton) mBaseView.findViewById(R.id.rbtnCreateAccount);
        rbtnCreateAccount.setTypeface(faceRobotoRegular);
        final CheckBox chkAcceptTerms = (CheckBox) mBaseView.findViewById(R.id.chkAcceptTerms);
        chkAcceptTerms.setTypeface(faceRobotoRegular);
        mEmailView = (AutoCompleteTextView) mBaseView.findViewById(R.id.emailInput);
        populateAutoComplete();
        final EditText editTextPasswd = (EditText) mBaseView.findViewById(R.id.editTextPasswd);
        final Button btnLinkToExistingSocialAccount = (Button) mBaseView.findViewById(R.id.btnLinkToExistingSocialAccount);
        final Button btnCreateNewAccount = (Button) mBaseView.findViewById(R.id.btnCreateNewAccount);
        final TextView txtCreateNewAccount = (TextView) mBaseView.findViewById(R.id.txtCreateNewAccount);
        txtCreateNewAccount.setTypeface(faceRobotoRegular);

        txtSocialLoggedInAs.setText("You are logged in to " + socialServiceName + " as " +
                memberName + " ( " + memberEmail + " )");
        txtNoAccountForSocialLogin.setText("We do not have a BigBasket account with " +
                memberEmail);
        txtSocialChooseMethod.setText("To complete the " + socialServiceName + " account linking, "
                + "select from the two options mentioned below");
        rbtnLinkToExistingSocialAccount.setText("Link " + memberName + " " + socialServiceName + " account " +
                "with my existing BigBasket account mentioned below");
        rbtnCreateAccount.setText("I do not have a BigBasket account");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String currentCityName = preferences.getString(Constants.CITY, "");
        txtCreateNewAccount.setText("Create a new BigBasket account for " + currentCityName +
                " city and link it with my " + memberName + " " + socialServiceName + " account");

        final View layoutLoginInput = mBaseView.findViewById(R.id.layoutLoginInput);
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
                    onLinkToExistingAccount(email, password);
                }
            }
        });

        btnCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAccountButtonClicked();
            }
        });
    }

    public abstract void onLinkToExistingAccount(String email, String password);

    public abstract void onCreateNewAccountButtonClicked();
}
