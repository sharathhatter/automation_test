package com.bigbasket.mobileapp.fragment.account;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileOldApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UpdateProfileFragment extends BaseFragment implements PinCodeAware {

    private EditText editTextEmail, editTextFirstName, editTextLastName, editTextDob,
            editTextHouseAndDetails, editTextStreetDetails, editTextCity, editTextMobileNumber,
            editTextTelNumber, editTextResAndComplex, editTextLandmark, editTextPinCode;
    private AutoCompleteTextView editTextArea;
    private CheckBox chkReceivePromos;
    private ProgressBar progressBarUpdateProfile;
    private Button btnUpdate;
    private OTPDialog otpDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_update_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUpdateProfileActivity();
    }


    public void initiateUpdateProfileActivity() {
        final View view = getContentView();
        if (view == null) return;
        editTextEmail = (EditText) view.findViewById(R.id.editTextEmail);
        editTextEmail.setTypeface(faceRobotoRegular);

        editTextFirstName = (EditText) view.findViewById(R.id.editTextFirstName);
        editTextFirstName.setTypeface(faceRobotoRegular);

        editTextLastName = (EditText) view.findViewById(R.id.editTextLastName);
        editTextLastName.setTypeface(faceRobotoRegular);

        editTextDob = (EditText) view.findViewById(R.id.editTextDob);
        editTextDob.setTypeface(faceRobotoRegular);

        editTextHouseAndDetails = (EditText) view.findViewById(R.id.editTextHouseAndDetails);
        editTextHouseAndDetails.setTypeface(faceRobotoRegular);

        editTextStreetDetails = (EditText) view.findViewById(R.id.editTextStreetDetails);
        editTextStreetDetails.setTypeface(faceRobotoRegular);

        editTextArea = (AutoCompleteTextView) view.findViewById(R.id.editTextArea);
        editTextArea.setTypeface(faceRobotoRegular);

        editTextCity = (EditText) view.findViewById(R.id.editTextCity);
        editTextCity.setTypeface(faceRobotoRegular);

        editTextMobileNumber = (EditText) view.findViewById(R.id.editTextMobileNumber);
        editTextMobileNumber.setTypeface(faceRobotoRegular);

        InputFilter maxLengthFilter = new InputFilter.LengthFilter(10);
        editTextMobileNumber.setFilters(new InputFilter[]{maxLengthFilter});

        editTextTelNumber = (EditText) view.findViewById(R.id.editTextTelNumber);
        editTextTelNumber.setTypeface(faceRobotoRegular);

        editTextResAndComplex = (EditText) view.findViewById(R.id.editTextResAndComplex);
        editTextResAndComplex.setTypeface(faceRobotoRegular);

        editTextLandmark = (EditText) view.findViewById(R.id.editTextLandmark);
        editTextLandmark.setTypeface(faceRobotoRegular);

        editTextPinCode = (EditText) view.findViewById(R.id.editTextPinCode);
        editTextPinCode.setTypeface(faceRobotoRegular);

        chkReceivePromos = (CheckBox) view.findViewById(R.id.chkReceivePromos);
        progressBarUpdateProfile = (ProgressBar) view.findViewById(R.id.progressBarUpdateProfile);

        btnUpdate = (Button) view.findViewById(R.id.btnUpdateProfile);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdateAfterSuccessNumberValidation(null);
            }
        });

        ImageView imgCalc = (ImageView) view.findViewById(R.id.imgCalc);
        imgCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(view);
            }
        });

        if (((BaseActivity) getActivity()).getSystemAreaInfo()) {
            getAreaInfo();
        } else {
            loadMemberDetails();
        }
    }

    protected void getAreaInfo() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(this));
    }

    private void loadMemberDetails() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_SELECTED, null);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getMemberProfileData(new Callback<UpdateProfileOldApiResponse>() {
            @Override
            public void success(UpdateProfileOldApiResponse memberProfileDataCallback, Response response) {
                hideProgressDialog();
                if (memberProfileDataCallback.status.equals(Constants.OK)) {
                    UpdateProfileModel updateProfileModel = memberProfileDataCallback.memberDetails;
                    fillUpdateProfileData(updateProfileModel);
                } else {
                    int errorType = Integer.parseInt(memberProfileDataCallback.errorType);
                    switch (errorType) {
                        case ApiErrorCodes.INVALID_USER_PASSED:
                            showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                            break;
                        default:
                            handler.sendEmptyMessage(errorType, memberProfileDataCallback.message, true);
                            break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment(view);
        newFragment.show(getFragmentManager(), Constants.DATE_PICKER);
    }

    private void validateOtp(int otp_flag, String errorMsg) {
        switch (otp_flag) {
            case ApiErrorCodes.NUMBER_IN_USE:
                showErrorMsg(errorMsg != null ? errorMsg : getResources().getString(R.string.numberUsedByAnotherMember));
                break;
            case ApiErrorCodes.OTP_NEEDED:
                validateMobileNumber(false, errorMsg);
                break;
            case ApiErrorCodes.OTP_INVALID:
                validateMobileNumber(true, errorMsg);
                break;
        }
    }

    @Override
    public void onPinCodeFetchSuccess() {
        loadMemberDetails();
        ((BaseActivity) getActivity()).setAdapterArea(editTextArea, editTextPinCode);
        //initiateUpdateProfileActivity();
    }

    @Override
    public void onPinCodeFetchFailure() {
        loadMemberDetails();
        ((BaseActivity) getActivity()).setAdapterArea(editTextArea, editTextPinCode);
        //initiateUpdateProfileActivity();
    }

    public static class OTPDialog extends OTPValidationDialogFragment {
        private UpdateProfileFragment fragment;

        public OTPDialog() {
        }

        @SuppressLint("ValidFragment")
        public OTPDialog(BaseActivity baseActivity, UpdateProfileFragment fragment) {
            super(baseActivity, faceRobotoRegular);
            this.fragment = fragment;
        }

        @Override
        public void resendOrConfirmOTP(String otp) {
            fragment.btnUpdateAfterSuccessNumberValidation(otp);
        }
    }

    private void validateMobileNumber(boolean txtErrorValidateNumberVisibility, String errorMsg) {
        if (otpDialog == null)
            otpDialog = new OTPDialog((BaseActivity) getActivity(), this);
        if (otpDialog.isVisible()) {
            if (txtErrorValidateNumberVisibility) {
                otpDialog.showErrorText(errorMsg);
            }
            return;
        } else {
            otpDialog.show(getFragmentManager(), Constants.OTP_DIALOG_FLAG);
        }

    }


    private void updatePreferenceData() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefer.edit();

        ((BaseActivity) getActivity()).showToast(getString(R.string.profileUpdated));
        editor.putString(Constants.MEMBER_EMAIL_KEY, editTextEmail.getText().toString());
        editor.putString(Constants.FIRST_NAME_PREF, editTextFirstName.getText().toString());
        editor.putString(Constants.LASTNAME, editTextLastName.getText().toString());
        editor.putString(Constants.DOB_PREF, editTextDob.getText().toString());
        editor.putString(Constants.MOB_NUMBER_PREF, editTextMobileNumber.getText().toString());
        editor.putString(Constants.HOUSE_NO_PREF, editTextHouseAndDetails.getText().toString());
        editor.putString(Constants.AREA, editTextArea.getText().toString());
        editor.putString(Constants.CITY, editTextCity.getText().toString());
        editor.putString(Constants.PINCODE, editTextPinCode.getText().toString());
        editor.putString(Constants.MEMBER_FULL_NAME_KEY, editTextFirstName.getText().toString() + " " +
                editTextLastName.getText().toString());
        editor.putString(Constants.NEWS_PREF, String.valueOf(chkReceivePromos.isChecked()));
        editor.commit();
        AuthParameters.updateInstance(getActivity());
        finish();
    }

    private void fillUpdateProfileData(UpdateProfileModel updateProfileModel) {
        editTextEmail.setText(updateProfileModel.getEmail());
        editTextFirstName.setText(updateProfileModel.getFirstName());
        editTextLastName.setText(updateProfileModel.getLastName());
        editTextDob.setText(updateProfileModel.getDateOfBirth());
        editTextHouseAndDetails.setText(updateProfileModel.getHouseNumber());
        editTextStreetDetails.setText(updateProfileModel.getStreet());
        editTextCity.setText(updateProfileModel.getCityName());
        editTextMobileNumber.setText(updateProfileModel.getMobileNumber());
        editTextTelNumber.setText(updateProfileModel.getTelephoneNumber());
        editTextResAndComplex.setText(updateProfileModel.getResidentialComplex());
        editTextLandmark.setText(updateProfileModel.getLandmark());
        editTextPinCode.setText(updateProfileModel.getPincode());
        editTextArea.setText(updateProfileModel.getArea());
        ((BaseActivity) getActivity()).setAdapterArea(editTextArea, editTextPinCode);
    }

    private void setUpdateButtonInProgress() {
        btnUpdate.setText(getString(R.string.updating));
        progressBarUpdateProfile.setVisibility(View.VISIBLE);
        progressBarUpdateProfile.setOnClickListener(null);
    }

    private void resetUpdateButtonInProgress() {
        btnUpdate.setText(getString(R.string.UPDATE));
        progressBarUpdateProfile.setVisibility(View.GONE);
        progressBarUpdateProfile.setOnClickListener(null);
    }

    public void btnUpdateAfterSuccessNumberValidation(String otp_code) {
        editTextEmail.setError(null);
        editTextFirstName.setError(null);
        editTextLastName.setError(null);
        editTextArea.setError(null);
        editTextDob.setError(null);
        editTextPinCode.setError(null);
        editTextMobileNumber.setError(null);
        editTextHouseAndDetails.setError(null);

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
            cancel = true;
            focusView = editTextEmail;
            UIUtil.reportFormInputFieldError(editTextEmail, getString(R.string.error_field_required));
        }
        if (!UIUtil.isValidEmail(editTextEmail.getText().toString())) {
            UIUtil.reportFormInputFieldError(editTextEmail, getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(editTextFirstName.getText().toString())) {
            cancel = true;
            focusView = editTextFirstName;
            UIUtil.reportFormInputFieldError(editTextFirstName, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(editTextLastName.getText().toString())) {
            cancel = true;
            focusView = editTextLastName;
            UIUtil.reportFormInputFieldError(editTextLastName, getString(R.string.error_field_required));
        }

        if (TextUtils.isEmpty(editTextMobileNumber.getText().toString())) {
            cancel = true;
            focusView = editTextMobileNumber;
            UIUtil.reportFormInputFieldError(editTextMobileNumber, getString(R.string.error_field_required));
        }

        if (!TextUtils.isDigitsOnly(editTextMobileNumber.getText().toString())) {
            UIUtil.reportFormInputFieldError(editTextMobileNumber, getString(R.string.error_invalid_mobile_number));
            focusView = editTextMobileNumber;
            cancel = true;
        }
        if (editTextMobileNumber.getText().toString().length() > 10) {
            UIUtil.reportFormInputFieldError(editTextMobileNumber, getString(R.string.error_mobile_number_less_digits));
            focusView = editTextMobileNumber;
            cancel = true;
        }
        if (TextUtils.isEmpty(editTextHouseAndDetails.getText().toString())) {
            cancel = true;
            focusView = editTextHouseAndDetails;
            UIUtil.reportFormInputFieldError(editTextHouseAndDetails, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(editTextArea.getText().toString())) {
            cancel = true;
            focusView = editTextArea;
            UIUtil.reportFormInputFieldError(editTextArea, getString(R.string.error_field_required));

        }
        if (TextUtils.isEmpty(editTextCity.getText().toString())) {
            cancel = true;
            focusView = editTextCity;
            UIUtil.reportFormInputFieldError(editTextCity, getString(R.string.error_field_required));

        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }
        if (checkInternetConnection()) {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String cityId = prefer.getString(Constants.CITY_ID, "");
            final JSONObject user_details = new JSONObject();
            try {
                user_details.put(Constants.EMAIL, editTextEmail.getText().toString());
                user_details.put(Constants.FIRSTNAME, editTextFirstName.getText().toString());
                user_details.put(Constants.LASTNAME, editTextLastName.getText().toString());
                user_details.put(Constants.DATE_OF_BIRTH, editTextDob.getText().toString());
                user_details.put(Constants.MOBILE_NUMBER, editTextMobileNumber.getText().toString());
                user_details.put(Constants.TELEPHONE_NUMBER, editTextTelNumber.getText().toString());
                user_details.put(Constants.HOUSE_NO, editTextHouseAndDetails.getText().toString());
                user_details.put(Constants.STREET, editTextStreetDetails.getText().toString());
                user_details.put(Constants.AREA, editTextArea.getText().toString());
                user_details.put(Constants.RES_CMPLX, editTextResAndComplex.getText().toString());
                user_details.put(Constants.LANDMARK, editTextLandmark.getText().toString());
                user_details.put(Constants.CITY_ID, cityId);
                user_details.put(Constants.CITY, editTextCity.getText().toString());
                user_details.put(Constants.PIN_CODE, editTextPinCode.getText().toString());
                user_details.put(Constants.NEWSPAPER_SUBSCRIPTION, chkReceivePromos.isChecked());
                if (otp_code != null) {
                    user_details.put(Constants.OTP_CODE, otp_code);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpdateButtonInProgress();
            postUserDetails(user_details.toString());
        } else {
            ((BaseActivity) getActivity()).showAlertDialogFinish(null, getString(R.string.checkinternet));
        }
    }

    private void postUserDetails(String userDetails) {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.setUserDetailsData(userDetails, new Callback<UpdateProfileOldApiResponse>() {
            @Override
            public void success(UpdateProfileOldApiResponse memberProfileDataCallback, Response response) {
                hideProgressDialog();
                if (memberProfileDataCallback.status.equals(Constants.OK)) {
                    if (otpDialog != null && otpDialog.isVisible()) {
                        otpDialog.dismiss();
                        BaseActivity.hideKeyboard(((BaseActivity) getActivity()), otpDialog.getView());
                    }
                    resetUpdateButtonInProgress();
                    updatePreferenceData();
                    trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_SUCCESS, null);

                } else {
                    int errorCode = memberProfileDataCallback.getErrorTypeAsInt();
                    if (errorCode == ApiErrorCodes.NUMBER_IN_USE ||
                            errorCode == ApiErrorCodes.OTP_NEEDED ||
                            errorCode == ApiErrorCodes.OTP_INVALID) {
                        String errorMsg = memberProfileDataCallback.message;
                        validateOtp(errorCode, errorMsg);
                    } else {
                        handler.sendEmptyMessage(errorCode);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(TrackEventkeys.UPDATE_PROFILE_FAILURE_REASON, memberProfileDataCallback.message);
                        trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_FAILED, map);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                showErrorMsg(getString(R.string.server_error));
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.UPDATE_PROFILE_FAILURE_REASON, error.toString());
                trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_FAILED, map);
            }
        });
    }


    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutUpdateProfile) : null;
    }

    @Override
    public String getTitle() {
        return "Update Profile";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return UpdateProfileFragment.class.getName();
    }
}
