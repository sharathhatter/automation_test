package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileApiResponse;
import com.bigbasket.mobileapp.fragment.account.DatePickerFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.OtpValidationHelper;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit.Call;

public class UpdateProfileActivity extends BackButtonActivity implements OtpDialogAware {
    private EditText editTextEmail, editTextFirstName, editTextLastName, editTextDob,
            editTextMobileNumber,
            editTextTelNumber;
    private CheckBox chkReceivePromos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateProfileModel updateProfileModel = getIntent().getParcelableExtra(Constants.UPDATE_PROFILE_OBJ);
        if (updateProfileModel == null) return;
        setTitle(getString(R.string.update_profile));
        initiateUpdateProfileActivity(updateProfileModel);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_update_profile;
    }

    private void initiateUpdateProfileActivity(UpdateProfileModel updateProfileModel) {
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextEmail.setTypeface(faceRobotoRegular);
        editTextEmail.setText(updateProfileModel.getEmail());

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextFirstName.setTypeface(faceRobotoRegular);
        editTextFirstName.setNextFocusDownId(R.id.editTextLastName);
        editTextFirstName.setText(updateProfileModel.getFirstName());

        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextLastName.setTypeface(faceRobotoRegular);
        editTextLastName.setText(updateProfileModel.getLastName());

        editTextDob = (EditText) findViewById(R.id.editTextDob);
        editTextDob.setTypeface(faceRobotoRegular);
        editTextDob.setText(updateProfileModel.getDateOfBirth());

        editTextMobileNumber = (EditText) findViewById(R.id.editTextMobileNumber);
        editTextMobileNumber.setTypeface(faceRobotoRegular);
        editTextMobileNumber.setText(updateProfileModel.getMobileNumber());

        InputFilter maxLengthFilter = new InputFilter.LengthFilter(10);
        editTextMobileNumber.setFilters(new InputFilter[]{maxLengthFilter});

        editTextTelNumber = (EditText) findViewById(R.id.editTextTelNumber);
        editTextTelNumber.setTypeface(faceRobotoRegular);
        editTextTelNumber.setText(updateProfileModel.getTelephoneNumber());

        chkReceivePromos = (CheckBox) findViewById(R.id.chkReceivePromos);
        chkReceivePromos.setChecked(updateProfileModel.isNewPaperSubscription());

        Button txtSave = (Button) findViewById(R.id.txtUpdateProfile);
        txtSave.setTypeface(faceRobotoMedium);
        txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateOtp(null, false);
            }
        });

        ImageView imgCalc = (ImageView) findViewById(R.id.imgCalc);
        imgCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(getContentView());
            }
        });
        trackEvent(TrackingAware.UPDATE_PROFILE_SHOWN, null);
    }

    private void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment(view);
        newFragment.show(getSupportFragmentManager(), Constants.DATE_PICKER);
    }

    private void handleMessage(int otp_flag, String errorMsg, boolean isResendOtpRequested) {
        switch (otp_flag) {
            case ApiErrorCodes.NUMBER_IN_USE:
                showAlertDialog(errorMsg != null ? errorMsg : getResources().getString(R.string.numberUsedByAnotherMember));
                break;
            case ApiErrorCodes.OTP_NEEDED:
                if (isResendOtpRequested) {
                    showToast(getString(R.string.resendOtpMsg));
                }
                OtpValidationHelper.requestOtpUI(this);
                break;
            case ApiErrorCodes.OTP_INVALID:
                OtpValidationHelper.reportError(this, errorMsg);
                break;
        }
    }

    private void updatePreferenceData() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefer.edit();

        showToast(getString(R.string.profileUpdated));
        editor.putString(Constants.MEMBER_EMAIL_KEY, editTextEmail.getText().toString());
        editor.putString(Constants.FIRST_NAME_PREF, editTextFirstName.getText().toString());
        editor.putString(Constants.LASTNAME, editTextLastName.getText().toString());
        editor.putString(Constants.DOB_PREF, editTextDob.getText().toString());
        editor.putString(Constants.MOB_NUMBER_PREF, editTextMobileNumber.getText().toString());
        editor.putString(Constants.MEMBER_FULL_NAME_KEY, editTextFirstName.getText().toString() + " " +
                editTextLastName.getText().toString());
        editor.putString(Constants.NEWS_PREF, String.valueOf(chkReceivePromos.isChecked()));
        editor.commit();
        AuthParameters.reset();
        setResultCodeOnProfileUpdate();
    }

    private void setResultCodeOnProfileUpdate() {
        setResult(NavigationCodes.ACCOUNT_UPDATED, null);
        finish();
    }

    @Override
    public void validateOtp(String otpCode, boolean isResendOtpRequested) {
        btnUpdateAfterSuccessNumberValidation(otpCode, isResendOtpRequested);
    }

    private void btnUpdateAfterSuccessNumberValidation(String otpCode, boolean isResendOtpRequested) {
        final View view = getContentView();
        if (view == null) return;

        TextInputLayout textInputEmail = (TextInputLayout) view.findViewById(R.id.textInputEmail);
        TextInputLayout textInputFirstName = (TextInputLayout) view.findViewById(R.id.textInputFirstName);
        TextInputLayout textInputLastName = (TextInputLayout) view.findViewById(R.id.textInputLastName);
        TextInputLayout textInputMobileNumber = (TextInputLayout) view.findViewById(R.id.textInputMobileNumber);
        TextInputLayout textInputDob = (TextInputLayout) view.findViewById(R.id.textInputDob);

        UIUtil.resetFormInputField(textInputEmail);
        UIUtil.resetFormInputField(textInputFirstName);
        UIUtil.resetFormInputField(textInputLastName);
        UIUtil.resetFormInputField(textInputMobileNumber);
        UIUtil.resetFormInputField(textInputDob);

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
            cancel = true;
            focusView = editTextEmail;
            UIUtil.reportFormInputFieldError(textInputEmail, getString(R.string.error_field_required));
        }

        if (!UIUtil.isValidEmail(editTextEmail.getText().toString())) {
            UIUtil.reportFormInputFieldError(textInputEmail, getString(R.string.error_invalid_email));
            if (focusView == null) focusView = editTextEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(editTextFirstName.getText().toString())) {
            cancel = true;
            if (focusView == null) focusView = editTextFirstName;
            UIUtil.reportFormInputFieldError(textInputFirstName, getString(R.string.error_field_required));
        }

        if (!UIUtil.isAlphaString(editTextFirstName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextFirstName;
            UIUtil.reportFormInputFieldError(textInputFirstName, getString(R.string.error_field_name));
        }

        if (TextUtils.isEmpty(editTextLastName.getText().toString())) {
            cancel = true;
            if (focusView == null) focusView = editTextLastName;
            UIUtil.reportFormInputFieldError(textInputLastName, getString(R.string.error_field_required));
        }

        if (!UIUtil.isAlphaString(editTextLastName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextLastName;
            UIUtil.reportFormInputFieldError(textInputLastName, getString(R.string.error_field_name));
        }

        if (TextUtils.isEmpty(editTextMobileNumber.getText().toString())) {
            cancel = true;
            if (focusView == null) focusView = editTextMobileNumber;
            UIUtil.reportFormInputFieldError(textInputMobileNumber, getString(R.string.error_field_required));
        }


        if (!TextUtils.isDigitsOnly(editTextMobileNumber.getText().toString())) {
            UIUtil.reportFormInputFieldError(textInputMobileNumber, getString(R.string.error_invalid_mobile_number));
            if (focusView == null) focusView = editTextMobileNumber;
            cancel = true;
        }

        if (editTextMobileNumber.getText().toString().length() != 10) {
            UIUtil.reportFormInputFieldError(textInputMobileNumber, getString(R.string.error_mobile_number_less_digits));
            if (focusView == null) focusView = editTextMobileNumber;
            cancel = true;
        }

        if (!TextUtils.isEmpty(editTextDob.getText().toString()) &&
                !UIUtil.isValidDOB(editTextDob.getText().toString()) && !cancel) {
            UIUtil.reportFormInputFieldError(textInputDob, getString(R.string.error_dob_message));
            focusView = editTextDob;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            hideKeyboard(this, focusView);
            return;
        }
        if (checkInternetConnection()) {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
            String cityId = prefer.getString(Constants.CITY_ID, "");
            final JSONObject user_details = new JSONObject();
            try {
                user_details.put(Constants.EMAIL, editTextEmail.getText().toString());
                user_details.put(Constants.FIRSTNAME, editTextFirstName.getText().toString());
                user_details.put(Constants.LASTNAME, editTextLastName.getText().toString());
                user_details.put(Constants.DATE_OF_BIRTH, editTextDob.getText().toString());
                user_details.put(Constants.MOBILE_NUMBER, editTextMobileNumber.getText().toString());
                user_details.put(Constants.TELEPHONE_NUMBER, editTextTelNumber.getText().toString());
                user_details.put(Constants.CITY_ID, cityId);
                user_details.put(Constants.NEWSPAPER_SUBSCRIPTION, chkReceivePromos.isChecked());
                if (otpCode != null) {
                    user_details.put(Constants.OTP_CODE, otpCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            postUserDetails(user_details.toString(), otpCode != null, isResendOtpRequested);
        } else {
            handler.sendOfflineError();
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.ENABLED, chkReceivePromos.isChecked() ? TrackEventkeys.YES :
                TrackEventkeys.NO);
        trackEvent(TrackingAware.PROMO_MAILER_ENABLED, map);
    }

    private void postUserDetails(String userDetails, final boolean hasOTP, final boolean isResendOtpRequested) {
        if (!DataUtil.isInternetAvailable(this)) {
            handler.sendOfflineError(false);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(isResendOtpRequested ? getString(R.string.resending_otp) : getString(R.string.please_wait));
        Call<ApiResponse<UpdateProfileApiResponse>> call = bigBasketApiService.setUserDetailsData(userDetails);
        call.enqueue(new BBNetworkCallback<ApiResponse<UpdateProfileApiResponse>>(this) {
            @Override
            public void onSuccess(ApiResponse<UpdateProfileApiResponse> memberProfileDataCallback) {
                if (memberProfileDataCallback.status == 0) {
                    OtpValidationHelper.dismiss();
                    updatePreferenceData();
                } else {
                    int errorCode = memberProfileDataCallback.status;
                    if (errorCode == ApiErrorCodes.NUMBER_IN_USE ||
                            errorCode == ApiErrorCodes.OTP_NEEDED ||
                            errorCode == ApiErrorCodes.OTP_INVALID) {
                        if (hasOTP) {
                            logUpdateProfileEvent(memberProfileDataCallback.message,
                                    TrackingAware.OTP_SUBMIT_BTN_CLICKED);
                        }
                        handleMessage(errorCode, memberProfileDataCallback.message, isResendOtpRequested);
                    } else {
                        handler.sendEmptyMessage(errorCode, memberProfileDataCallback.message);
                        logUpdateProfileEvent(memberProfileDataCallback.message,
                                TrackingAware.UPDATE_PROFILE_SUBMIT_BTN_CLICKED);
                        OtpValidationHelper.dismiss();
                    }
                }
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

            @Override
            public void onFailure(int httpErrorCode, String msg) {
                super.onFailure(httpErrorCode, msg);
                logUpdateProfileEvent(msg, TrackingAware.UPDATE_PROFILE_SUBMIT_BTN_CLICKED);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                logUpdateProfileEvent(getString(R.string.networkError), TrackingAware.UPDATE_PROFILE_SUBMIT_BTN_CLICKED);
            }
        });
    }

    private void logUpdateProfileEvent(String message, String eventName) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.FAILURE_REASON, message);
        trackEvent(eventName, map);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (editTextFirstName != null && getCurrentActivity() != null) {
            BaseActivity.hideKeyboard(getCurrentActivity(), editTextFirstName);
        }
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean handled = OtpValidationHelper.onRequestPermissionsResult(this, requestCode,
                permissions, grantResults);
        if (!handled) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNoFragmentsInLayout() {
        OtpValidationHelper.onDestroy();
        // No need to finish this Activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OtpValidationHelper.onDestroy();
    }
}
