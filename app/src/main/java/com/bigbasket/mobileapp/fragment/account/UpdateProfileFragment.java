package com.bigbasket.mobileapp.fragment.account;

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

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileOldApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
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
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UpdateProfileFragment extends BaseFragment implements PinCodeAware, OtpDialogAware {

    private EditText editTextEmail, editTextFirstName, editTextLastName, editTextDob,
            editTextHouseAndDetails, editTextStreetDetails, editTextCity, editTextMobileNumber,
            editTextTelNumber, editTextResAndComplex, editTextLandmark, editTextPinCode;
    private AutoCompleteTextView editTextArea;
    private CheckBox chkReceivePromos;
    private OTPValidationDialogFragment otpValidationDialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_update_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UpdateProfileModel  updateProfileModel = getArguments().getParcelable(Constants.UPDATE_PROFILE_OBJ);
        if(updateProfileModel==null) return;
        initiateUpdateProfileActivity(updateProfileModel);
    }


    public void initiateUpdateProfileActivity(UpdateProfileModel  updateProfileModel) {
        final View view = getContentView();
        if (view == null) return;
        editTextEmail = (EditText) view.findViewById(R.id.editTextEmail);
        editTextEmail.setTypeface(faceRobotoRegular);
        editTextEmail.setText(updateProfileModel.getEmail());

        editTextFirstName = (EditText) view.findViewById(R.id.editTextFirstName);
        editTextFirstName.setTypeface(faceRobotoRegular);
        editTextFirstName.setNextFocusDownId(R.id.editTextLastName);
        editTextFirstName.setText(updateProfileModel.getFirstName());

        editTextLastName = (EditText) view.findViewById(R.id.editTextLastName);
        editTextLastName.setTypeface(faceRobotoRegular);
        editTextLastName.setText(updateProfileModel.getLastName());

        editTextDob = (EditText) view.findViewById(R.id.editTextDob);
        editTextDob.setTypeface(faceRobotoRegular);
        editTextDob.setText(updateProfileModel.getDateOfBirth());

        editTextHouseAndDetails = (EditText) view.findViewById(R.id.editTextHouseAndDetails);
        editTextHouseAndDetails.setTypeface(faceRobotoRegular);
        editTextHouseAndDetails.setText(updateProfileModel.getHouseNumber());

        editTextStreetDetails = (EditText) view.findViewById(R.id.editTextStreetDetails);
        editTextStreetDetails.setTypeface(faceRobotoRegular);
        editTextStreetDetails.setText(updateProfileModel.getStreet());

        editTextArea = (AutoCompleteTextView) view.findViewById(R.id.editTextArea);
        editTextArea.setTypeface(faceRobotoRegular);
        editTextArea.setText(updateProfileModel.getArea());

        editTextCity = (EditText) view.findViewById(R.id.editTextCity);
        editTextCity.setTypeface(faceRobotoRegular);
        editTextCity.setText(updateProfileModel.getCityName());

        editTextMobileNumber = (EditText) view.findViewById(R.id.editTextMobileNumber);
        editTextMobileNumber.setTypeface(faceRobotoRegular);
        editTextMobileNumber.setText(updateProfileModel.getMobileNumber());

        InputFilter maxLengthFilter = new InputFilter.LengthFilter(10);
        editTextMobileNumber.setFilters(new InputFilter[]{maxLengthFilter});

        editTextTelNumber = (EditText) view.findViewById(R.id.editTextTelNumber);
        editTextTelNumber.setTypeface(faceRobotoRegular);
        editTextTelNumber.setText(updateProfileModel.getTelephoneNumber());

        editTextResAndComplex = (EditText) view.findViewById(R.id.editTextResAndComplex);
        editTextResAndComplex.setTypeface(faceRobotoRegular);
        editTextResAndComplex.setText(updateProfileModel.getResidentialComplex());

        editTextLandmark = (EditText) view.findViewById(R.id.editTextLandmark);
        editTextLandmark.setTypeface(faceRobotoRegular);
        editTextLandmark.setText(updateProfileModel.getLandmark());

        editTextPinCode = (EditText) view.findViewById(R.id.editTextPinCode);
        editTextPinCode.setTypeface(faceRobotoRegular);
        editTextPinCode.setText(updateProfileModel.getPincode());

        chkReceivePromos = (CheckBox) view.findViewById(R.id.chkReceivePromos);

        Button btnUpdate = (Button) view.findViewById(R.id.btnUpdateProfile);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateOtp(null);
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
        }
        trackEvent(TrackingAware.UPDATE_PROFILE_SHOWN, null);
    }

    protected void getAreaInfo() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(this));
    }


    /**

    private void loadMemberDetails() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
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
                            Map<String, String> eventAttribs = new HashMap<>();
                            eventAttribs.put(TrackEventkeys.FAILURE_REASON, memberProfileDataCallback.message);
                            trackEvent(TrackingAware.UPDATE_PROFILE_GET_FAILED, eventAttribs);
                            break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                handler.handleRetrofitError(error, true);
                Map<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(TrackEventkeys.FAILURE_REASON, error.toString());
                trackEvent(TrackingAware.UPDATE_PROFILE_GET_FAILED, eventAttribs);
            }
        });
    }

     **/

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
        //loadMemberDetails();
        ((BaseActivity) getActivity()).setAdapterArea(editTextArea, editTextPinCode);
        //initiateUpdateProfileActivity();
    }

    @Override
    public void onPinCodeFetchFailure() {
        //loadMemberDetails();
        ((BaseActivity) getActivity()).setAdapterArea(editTextArea, editTextPinCode);
        //initiateUpdateProfileActivity();
    }

    private void validateMobileNumber(boolean txtErrorValidateNumberVisibility, String errorMsg) {
        if (otpValidationDialogFragment == null) {
            otpValidationDialogFragment = OTPValidationDialogFragment.newInstance(true);
        }
        if (otpValidationDialogFragment.isVisible()) {
            if (txtErrorValidateNumberVisibility) {
                otpValidationDialogFragment.showErrorText(errorMsg);
            }
        } else {
            otpValidationDialogFragment.setTargetFragment(this, 0);
            otpValidationDialogFragment.show(getFragmentManager(), Constants.OTP_DIALOG_FLAG);
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

    /**
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

     **/

    @Override
    public void validateOtp(String otpCode) {
        btnUpdateAfterSuccessNumberValidation(otpCode);
    }

    public void btnUpdateAfterSuccessNumberValidation(String otpCode) {
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
                if (otpCode != null) {
                    user_details.put(Constants.OTP_CODE, otpCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            postUserDetails(user_details.toString(), otpCode != null);
        } else {
            handler.sendOfflineError();
        }
    }

    private void postUserDetails(String userDetails, final boolean hasOTP) {
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
                    if (otpValidationDialogFragment != null && otpValidationDialogFragment.isVisible()) {
                        otpValidationDialogFragment.dismiss();
                        BaseActivity.hideKeyboard(((BaseActivity) getActivity()), otpValidationDialogFragment.getView());
                    }
                    updatePreferenceData();
                } else {
                    int errorCode = memberProfileDataCallback.getErrorTypeAsInt();
                    if (errorCode == ApiErrorCodes.NUMBER_IN_USE ||
                            errorCode == ApiErrorCodes.OTP_NEEDED ||
                            errorCode == ApiErrorCodes.OTP_INVALID) {
                        if (hasOTP)
                            logUpdateProfileEvent(memberProfileDataCallback.message,
                                    TrackingAware.OTP_SUBMIT_BTN_CLICKED);
                        validateOtp(errorCode, memberProfileDataCallback.message);
                    } else {
                        handler.sendEmptyMessage(errorCode, memberProfileDataCallback.message);
                        logUpdateProfileEvent(memberProfileDataCallback.message,
                                TrackingAware.UPDATE_PROFILE_SUBMIT_BTN_CLICKED);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                handler.handleRetrofitError(error);
                logUpdateProfileEvent(error.toString(), TrackingAware.UPDATE_PROFILE_SUBMIT_BTN_CLICKED);
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

    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.layoutUpdateProfile) : null;
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

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_UPDATE_PROFILE_SCREEN;
    }
}
