package com.bigbasket.mobileapp.activity.account.uiv3;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.handler.OnCompoundDrawableClickListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit.Call;

public class SignupActivity extends BackButtonActivity {

    // UI References
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    private boolean mIsPasswordVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_SIGNUP_SCREEN);
        setTitle(getString(R.string.signUpCapsVerb));

        renderSignUp();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_signup;
    }

    private void renderSignUp() {
        ((TextView) findViewById(R.id.txtOrSeparator)).setTypeface(faceRobotoRegular);
        ((TextView) findViewById(R.id.lblConnectUsing)).setTypeface(faceRobotoLight);
        mPasswordView = (EditText) findViewById(R.id.editTextPasswd);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    onRegisterButtonClicked();
                    hideKeyboard(getCurrentActivity(), mPasswordView);
                }
                return false;
            }
        });
        mFirstNameView = (EditText) findViewById(R.id.editTextFirstName);
        mFirstNameView.setNextFocusDownId(R.id.editTextLastName);
        mLastNameView = (EditText) findViewById(R.id.editTextLastName);
//        mRefCodeView = (EditText) base.findViewById(R.id.editTextRefCode);
        mEmailView = (EditText) findViewById(R.id.emailInput);

        Button btnSignUp = (Button) findViewById(R.id.btnRegister);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterButtonClicked();
            }
        });
        btnSignUp.setTypeface(faceRobotoRegular);

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

        setUpSocialButtons(findViewById(R.id.plus_sign_in_button),
                findViewById(R.id.btnFBLogin));


        showLocationView();
        trackEvent(TrackingAware.REGISTRATION_PAGE_SHOWN, null);
    }

    private void showLocationView() {
        EditText editTextCurrentLocation = (EditText) findViewById(R.id.editTextCurrentLocation);
        TextView txtChooseLocation = (TextView) findViewById(R.id.txtChooseLocation);
        TextInputLayout textInputCurrentLocation = (TextInputLayout) findViewById(R.id.textInputCurrentLocation);

        final ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(this).getAddressSummaries();
        if (addressSummaries != null && addressSummaries.size() > 0) {
            editTextCurrentLocation.setText(addressSummaries.get(0).toStringSameLine());
            editTextCurrentLocation.setTypeface(faceRobotoRegular);
            editTextCurrentLocation.setOnTouchListener(new OnCompoundDrawableClickListener(OnCompoundDrawableClickListener.DRAWABLE_RIGHT) {
                @Override
                public void onRightDrawableClicked() {
                    showChangeCity(false, TrackEventkeys.NC_SIGNUP_SCREEN, true);
                }

                @Override
                public void onLeftDrawableClicked() {

                }
            });
            editTextCurrentLocation.setVisibility(View.VISIBLE);
            textInputCurrentLocation.setVisibility(View.VISIBLE);
            txtChooseLocation.setVisibility(View.GONE);
        } else {
            textInputCurrentLocation.setVisibility(View.GONE);
            editTextCurrentLocation.setVisibility(View.GONE);
            txtChooseLocation.setVisibility(View.VISIBLE);
            txtChooseLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeCity(true, TrackEventkeys.NC_SIGNUP_SCREEN, true);
                }
            });
        }
    }

    private void onRegisterButtonClicked() {
        trackEvent(TrackingAware.REGISTER_BTN_CLICK, null);


        TextInputLayout textInputEmail = (TextInputLayout) findViewById(R.id.textInputEmail);
        TextInputLayout textInputPasswd = (TextInputLayout) findViewById(R.id.textInputPasswd);
        TextInputLayout textInputFirstName = (TextInputLayout) findViewById(R.id.textInputFirstName);
        TextInputLayout textInputLastName = (TextInputLayout) findViewById(R.id.textInputLastName);

        UIUtil.resetFormInputField(textInputEmail);
        UIUtil.resetFormInputField(textInputPasswd);
        UIUtil.resetFormInputField(textInputFirstName);
        UIUtil.resetFormInputField(textInputLastName);
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
            UIUtil.reportFormInputFieldError(textInputPasswd, "Please enter your password");
            focusView = mPasswordView;
            cancel = true;
        } else if (password.length() < 4) {
            UIUtil.reportFormInputFieldError(textInputPasswd, getString(R.string.error_invalid_password));
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

        // Check for last name
        if (TextUtils.isEmpty(lastName)) {
            UIUtil.reportFormInputFieldError(textInputLastName, "Please provide your last-name");
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for first name
        if (TextUtils.isEmpty(firstName)) {
            UIUtil.reportFormInputFieldError(textInputFirstName, "Please provide your first-name");
            focusView = mFirstNameView;
            cancel = true;
        }

        final ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(this).getAddressSummaries();
        if (addressSummaries == null || addressSummaries.size() == 0) {
            showAlertDialog("Please choose a location!");
            return;
        }

        if (!UIUtil.isAlphaString(firstName.trim())) {
            cancel = true;
            if (focusView == null) focusView = mFirstNameView;
            UIUtil.reportFormInputFieldError(textInputFirstName, getString(R.string.error_field_name));
        }

        if (!UIUtil.isAlphaString(lastName.trim())) {
            cancel = true;
            if (focusView == null) focusView = mLastNameView;
            UIUtil.reportFormInputFieldError(textInputLastName, getString(R.string.error_field_name));
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
        userDetailsJsonObj.addProperty(Constants.NEWSLETTER_SUBSCRIPTION, true);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Call<ApiResponse<LoginApiResponse>> call = bigBasketApiService.registerMember(userDetailsJsonObj.toString());
        call.enqueue(new LoginApiResponseCallback(email, passwd, true,
                Constants.REGISTER_ACCOUNT_TYPE, null));
    }

    @Override
    public void onDataSynced() {
        super.onDataSynced();
        showLocationView();
        hideProgressDialog();
    }

    @Override
    public void onDataSyncFailure() {
        super.onDataSyncFailure();
        showLocationView();
        hideProgressDialog();
        showAlertDialog(getString(R.string.headingServerError), getString(R.string.server_error));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.LOCATION_CHOSEN) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

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
