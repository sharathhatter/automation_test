package com.bigbasket.mobileapp.activity.account.uiv3;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.handler.OnRightCompoundDrawableClicked;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class SignupActivity extends BackButtonActivity implements CityListDisplayAware {

    // UI References
    private ArrayList<City> mCities;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private int mSelectedCityIndx;
    private EditText mCityView;
    private EditText mEmailView;
    private boolean mIsPasswordVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_SINGUP_SCREEN);
        setTitle(getString(R.string.signUpCapsVerb));

        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_signup;
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        mCities = cities;

        ((TextView) findViewById(R.id.txtOrSeparator)).setTypeface(faceRobotoRegular);
        ((TextView) findViewById(R.id.lblConnectUsing)).setTypeface(faceRobotoLight);
        mPasswordView = (EditText) findViewById(R.id.editTextPasswd);
        mFirstNameView = (EditText) findViewById(R.id.editTextFirstName);
        mFirstNameView.setNextFocusDownId(R.id.editTextLastName);
        mLastNameView = (EditText) findViewById(R.id.editTextLastName);
//        mRefCodeView = (EditText) base.findViewById(R.id.editTextRefCode);
        mEmailView = (EditText) findViewById(R.id.emailInput);
        mCityView = (EditText) findViewById(R.id.editTextChooseCity);

        Button btnSignUp = (Button) findViewById(R.id.btnRegister);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterButtonClicked();
            }
        });
        btnSignUp.setTypeface(faceRobotoRegular);

        mPasswordView.setOnTouchListener(new OnRightCompoundDrawableClicked() {
            @Override
            public void onRightDrawableClicked() {
                mIsPasswordVisible = !mIsPasswordVisible;
                togglePasswordView(mPasswordView, mIsPasswordVisible);
            }
        });

        mCityView.setTypeface(faceRobotoRegular);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentCityName = preferences.getString(Constants.CITY, cities.get(0).getName());
        String cityIdStr = preferences.getString(Constants.CITY_ID, String.valueOf(cities.get(0).getId()));
        if (TextUtils.isEmpty(cityIdStr) || !TextUtils.isDigitsOnly(cityIdStr)) {
            cityIdStr = "1";
        }
        int cityId = Integer.parseInt(cityIdStr);
        for (int i = 0; i < mCities.size(); i++) {
            City city = mCities.get(i);
            if (cityId == city.getId()) {
                mSelectedCityIndx = i;
                break;
            }
        }
        mCityView.setText(currentCityName);
        mCityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseCityDialog();
            }
        });
        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btnFBLogin));

        trackEvent(TrackingAware.REGISTRATION_PAGE_SHOWN, null);
    }

    private void showChooseCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] cityNames = new String[mCities.size()];
        for (int i = 0; i < mCities.size(); i++) {
            cityNames[i] = mCities.get(i).getName();
        }
        builder.setTitle(getString(R.string.chooseCity))
                .setItems(cityNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedCityIndx = which;
                        mCityView.setText(mCities.get(mSelectedCityIndx).getName());
                    }
                });
        builder.create().show();
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

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            reportFormInputFieldError(mEmailView, "Please enter your e-mail address");
            focusView = mEmailView;
            cancel = true;
        } else if (!UIUtil.isValidEmail(email)) {
            reportFormInputFieldError(mEmailView, getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for last name
        if (TextUtils.isEmpty(lastName)) {
            reportFormInputFieldError(mLastNameView, "Please provide your last-name");
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for first name
        if (TextUtils.isEmpty(firstName)) {
            reportFormInputFieldError(mFirstNameView, "Please provide your first-name");
            focusView = mFirstNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error, don't sign-up
            focusView.requestFocus();
        } else {
            showProgress(true);
            startMemberRegistration(email, password, firstName, lastName);
        }
    }

    private void startMemberRegistration(String email, String passwd, String firstName,
                                         String lastName) {
        JsonObject userDetailsJsonObj = new JsonObject();
        userDetailsJsonObj.addProperty(Constants.EMAIL, email);
        userDetailsJsonObj.addProperty(Constants.FIRSTNAME, firstName);
        userDetailsJsonObj.addProperty(Constants.LASTNAME, lastName);
        userDetailsJsonObj.addProperty(Constants.PASSWORD, passwd);
        userDetailsJsonObj.addProperty(Constants.CITY_ID, mCities.get(mSelectedCityIndx).getId());
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
    protected void onPause() {
        super.onPause();
        if (mPasswordView != null) {
            hideKeyboard(this, mPasswordView);
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.REGISTER_MEMBER_SCREEN;
    }
}
