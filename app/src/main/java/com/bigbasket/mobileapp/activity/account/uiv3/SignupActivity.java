package com.bigbasket.mobileapp.activity.account.uiv3;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.CityDropDownAdapter;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class SignupActivity extends BackButtonActivity implements CityListDisplayAware {

    // UI References
    private ArrayList<City> mCities;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private Spinner mCitySpinner;
    private AutoCompleteTextView mEmailView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_signup;
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        mCities = cities;

        setTitle(getString(R.string.signUp));

        mPasswordView = (EditText) findViewById(R.id.editTextPasswd);
        mFirstNameView = (EditText) findViewById(R.id.editTextFirstName);
        mFirstNameView.setNextFocusDownId(R.id.editTextLastName);
        mLastNameView = (EditText) findViewById(R.id.editTextLastName);
//        mRefCodeView = (EditText) base.findViewById(R.id.editTextRefCode);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.emailInput);

        Button btnSignUp = (Button) findViewById(R.id.btnRegister);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterButtonClicked();
            }
        });
        btnSignUp.setTypeface(faceRobotoRegular);

        CheckBox chkShowPassword = (CheckBox) findViewById(R.id.chkShowPasswd);
        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                togglePasswordView(mPasswordView, isChecked);
            }
        });

        mCitySpinner = (Spinner) findViewById(R.id.spinnerCity);
        CityDropDownAdapter<City> cityDropDownAdapter =
                new CityDropDownAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityDropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCitySpinner.setAdapter(cityDropDownAdapter);

        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btnFBLogin));

        setTermsAndCondition((TextView) findViewById(R.id.txtSigninTermsAndCond), getString(R.string.byRegistering),
                getString(R.string.termsAndCondHeading), getString(R.string.authFooterSeparator), getString(R.string.privacyPolicy));
        populateAutoComplete(mEmailView);
        trackEvent(TrackingAware.REGISTRATION_PAGE_SHOWN, null);
    }

    public void onRegisterButtonClicked() {
        trackEvent(TrackingAware.REGISTER_BTN_CLICK, null);

        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
//        mRefCodeView.setError(null);

        boolean cancel = false;
        View focusView = null;


        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
//        String refCode = mRefCodeView.getText().toString().trim();

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

        if (cancel) {
            // There was an error, don't sign-up
            focusView.requestFocus();
        } else {
            showProgress(true);
            startMemberRegistration(email, password, firstName, lastName, null);
        }
    }

    private void startMemberRegistration(String email, String passwd, String firstName,
                                         String lastName, String refCode) {
        JsonObject userDetailsJsonObj = new JsonObject();
        userDetailsJsonObj.addProperty(Constants.EMAIL, email);
        userDetailsJsonObj.addProperty(Constants.FIRSTNAME, firstName);
        userDetailsJsonObj.addProperty(Constants.LASTNAME, lastName);
        userDetailsJsonObj.addProperty(Constants.PASSWORD, passwd);
        userDetailsJsonObj.addProperty(Constants.CITY_ID, mCities.get(mCitySpinner
                .getSelectedItemPosition()).getId());
        if (!TextUtils.isEmpty(refCode)) {
            userDetailsJsonObj.addProperty(Constants.REF_CODE, refCode);
        }
        userDetailsJsonObj.addProperty(Constants.NEWSLETTER_SUBSCRIPTION, true);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        bigBasketApiService.registerMember(userDetailsJsonObj.toString(),
                new LoginApiResponseCallback(email, passwd, true, Constants.REGISTER_ACCOUNT_TYPE));
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        if (show) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.REGISTER_MEMBER_SCREEN;
    }
}
