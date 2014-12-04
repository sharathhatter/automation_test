package com.bigbasket.mobileapp.fragment.account;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.UIUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UpdateProfileFragment extends BaseFragment implements PinCodeAware {

    private EditText editTextEmail, editTextFirstName, editTextLastName, editTextDob,
            editTextHouseAndDetails, editTextStreetDetails, editTextCity, editTextMobileNumber,
            editTextTelNumber, editTextResAndComplex, editTextLandmark, editTextPinCode;
    private AutoCompleteTextView editTextArea;
    private ImageView imgEmailErr, imgFirstNameErr, imgLastNameErr, imgMobileNumberErr, imgHouseAndDetailsErr,
            imgAreaErr, imgCityErr, imgPinCodeErr;
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
        getAreaInfo();
//        if (((BaseActivity) getActivity()).getSystemAreaInfo()) {
//            getAreaInfo();
//        }else {
//            initiateUpdateProfileActivity();
//        }
    }


    public void initiateUpdateProfileActivity() {
        final View view = getView();
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

        imgEmailErr = (ImageView) view.findViewById(R.id.imgEmailErr);
        imgEmailErr.setVisibility(View.GONE);

        imgFirstNameErr = (ImageView) view.findViewById(R.id.imgFirstNameErr);
        imgFirstNameErr.setVisibility(View.GONE);

        imgLastNameErr = (ImageView) view.findViewById(R.id.imgLastNameErr);
        imgLastNameErr.setVisibility(View.GONE);

        imgMobileNumberErr = (ImageView) view.findViewById(R.id.imgMobileNumberErr);
        imgMobileNumberErr.setVisibility(View.GONE);


        imgHouseAndDetailsErr = (ImageView) view.findViewById(R.id.imgHouseAndDetailsErr);
        imgHouseAndDetailsErr.setVisibility(View.GONE);

        imgAreaErr = (ImageView) view.findViewById(R.id.imgAreaErr);
        imgAreaErr.setVisibility(View.GONE);

        imgCityErr = (ImageView) view.findViewById(R.id.imgCityErr);
        imgCityErr.setVisibility(View.GONE);

        imgPinCodeErr = (ImageView) view.findViewById(R.id.imgPinCodeErr);
        imgPinCodeErr.setVisibility(View.GONE);

        loadMemberDetails();
//        if (checkInternetConnection()) {
//            if (((BaseActivity) getActivity()).getSystemAreaInfo()) {
//                getAreaInfo();
//                //startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_AREA_INFO, null, false, true, null);
//            } else {
//                loadMemberDetails();
//            }
//
//        } else {
//            ((BaseActivity) getActivity()).showAlertDialogFinish(getActivity(), null, getString(R.string.checkinternet));
//        }
    }

    protected void getAreaInfo() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(this));
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, final Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.ERROR_AREA_PIN_CODE:
                    finish(); //todo check for fragment finish
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }


    /*
    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.UPDATE_PROFILE) && !httpOperationResult.isPost()) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    JsonObject memberDetailJsonobj = responseJsonObj.get(Constants.MEMBER_DETAILS).getAsJsonObject();
                    UpdateProfileModel updateProfileModel = ParserUtil.parseUpdateProfileData(memberDetailJsonobj);
                    fillUpdateProfileData(updateProfileModel);
                    break;
                case Constants.ERROR:
                    //TODO : Replace with handler
                    String errorType = responseJsonObj.get(Constants.ERROR_TYPE).getAsString();
                    switch (errorType) {
                        case Constants.INVALID_USER_PASS:
                            showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                            break;
                        default:
                            showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
                            break;
                    }
                    break;
            }
        } else if (httpOperationResult.getUrl().contains(Constants.UPDATE_PROFILE) && httpOperationResult.isPost()) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    if (otpDialog != null && otpDialog.isVisible()) {
                        otpDialog.dismiss();
                        BaseActivity.hideKeyboard(((BaseActivity) getActivity()), otpDialog.getView());
                    }
                    resetUpdateButtonInProgress();
                    updatePreferenceData();

                    break;
                case Constants.ERROR:
                    int errorCode = responseJsonObj.get(Constants.ERROR_TYPE).getAsInt();
                    if (errorCode == Constants.NUMBER_USED_BY_ANOTHER_MEMBER ||
                            errorCode == Constants.OPT_NEEDED ||
                            errorCode == Constants.INVALID_OTP) {
                        String errorMsg = responseJsonObj.get(Constants.MESSAGE).getAsString();
                        validateOtp(errorCode, errorMsg);
                    } else {
                        showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR)); //todo error handler
                    }
                    break;
            }
        } else if (httpOperationResult.getUrl().contains(Constants.GET_AREA_INFO)) {
            int j = 0;
            AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(getActivity());
            areaPinInfoAdapter.deleteTable();
            int responseCode = httpOperationResult.getResponseCode();
            if (responseCode == Constants.successRespCode) {
                Log.d("Response Code", "" + responseCode);
                try {
                    if (httpOperationResult.getReponseString() != null) {
                        JSONObject responseJSONObject = new JSONObject(httpOperationResult.getReponseString());

                        JSONObject responseJSON = responseJSONObject.getJSONObject(Constants.RESPONSE);
                        JSONObject pinCodeObj = responseJSON.getJSONObject(Constants.PIN_CODE_MAP);

                        @SuppressWarnings("unchecked") Iterator<String> myIter = pinCodeObj.keys();
                        String area1[] = new String[pinCodeObj.length()];
                        while (myIter.hasNext()) {
                            area1[j] = myIter.next();
                            for (int i = 0; i < pinCodeObj.getJSONArray(area1[j]).length(); i++) {
                                String areaName = String.valueOf(pinCodeObj.getJSONArray(area1[j]).get(i));
                                areaPinInfoAdapter.insert(areaName.toLowerCase(), String.valueOf(pinCodeObj.names().get(j)));
                            }
                            j++;
                        }
                    }
                    loadMemberDetails();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR)); // todo  error handling
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    */

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
                    String errorType = memberProfileDataCallback.errorType;
                    switch (errorType) {
                        case Constants.INVALID_USER_PASS:
                            showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                            break;
                        default:
                            showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
                            break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                showErrorMsg(getString(R.string.server_error));
            }
        });

//
//        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.UPDATE_PROFILE,
//                null, false, false, getString(R.string.please_wait), null);
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment(view);
        newFragment.show(getFragmentManager(), Constants.DATE_PICKER);
    }

    private void validateOtp(int otp_flag, String errorMsg) {
        switch (otp_flag) {
            case Constants.NUMBER_USED_BY_ANOTHER_MEMBER:
                showErrorMsg(errorMsg != null ? errorMsg : getResources().getString(R.string.numberUsedByAnotherMember));
                break;
            case Constants.OPT_NEEDED:
                validateMobileNumber(false, errorMsg);
                break;
            case Constants.INVALID_OTP:
                validateMobileNumber(true, errorMsg);
                break;
        }
    }

    @Override
    public void onPinCodeFetchSuccess() {
        initiateUpdateProfileActivity();
    }

    @Override
    public void onPinCodeFetchFailure() {
        initiateUpdateProfileActivity();
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

    private boolean validateFields() {
        // Validation
        ArrayList<String> missingFields = new ArrayList<>();
        if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
            missingFields.add(getString(R.string.email));
            imgEmailErr.setVisibility(View.VISIBLE);
        } else {
            imgEmailErr.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(editTextFirstName.getText().toString())) {
            missingFields.add(getString(R.string.firstName));
            imgFirstNameErr.setVisibility(View.VISIBLE);
        } else {
            imgFirstNameErr.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(editTextLastName.getText().toString())) {
            missingFields.add(getString(R.string.lastName));
            imgLastNameErr.setVisibility(View.VISIBLE);
        } else {
            imgLastNameErr.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(editTextMobileNumber.getText().toString())) {
            missingFields.add(getString(R.string.mobileNumber));
            imgMobileNumberErr.setVisibility(View.VISIBLE);
        } else {
            imgMobileNumberErr.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(editTextMobileNumber.getText().toString())) {
            missingFields.add(getString(R.string.mobileNumber));
            imgMobileNumberErr.setVisibility(View.VISIBLE);
        } else {
            imgMobileNumberErr.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(editTextHouseAndDetails.getText().toString())) {
            missingFields.add(getString(R.string.HNDetails));
            imgHouseAndDetailsErr.setVisibility(View.VISIBLE);
        } else {
            imgHouseAndDetailsErr.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(editTextArea.getText().toString())) {
            missingFields.add(getString(R.string.area));
            imgAreaErr.setVisibility(View.VISIBLE);
        } else {
            imgAreaErr.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(editTextCity.getText().toString())) {
            missingFields.add(getString(R.string.city));
            imgAreaErr.setVisibility(View.VISIBLE);
        } else {
            imgAreaErr.setVisibility(View.GONE);
        }
        int missingFieldsListSize = missingFields.size();
        if (missingFieldsListSize > 0) {
            String msg;
            if (missingFieldsListSize == 1) {
                msg = missingFields.get(0) + " is mandatory";
            } else {
                msg = "Following fields are mandatory: " + UIUtil.sentenceJoin(missingFields);
            }
            showErrorMsg(msg);
            return false;
        }
        return true;
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
        if (!validateFields())
            return;
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
//
//            startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.UPDATE_PROFILE,
//                    new HashMap<String, String>() {
//                        {
//                            put(Constants.USER_DETAILS, user_details.toString());
//                        }
//                    }, true, false, null);

        } else {
            ((BaseActivity) getActivity()).showAlertDialogFinish(getActivity(), null, getString(R.string.checkinternet));
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

                } else {
                    int errorCode = Integer.valueOf(memberProfileDataCallback.errorType);
                    if (errorCode == Constants.NUMBER_USED_BY_ANOTHER_MEMBER ||
                            errorCode == Constants.OPT_NEEDED ||
                            errorCode == Constants.INVALID_OTP) {
                        String errorMsg = memberProfileDataCallback.message;
                        validateOtp(errorCode, errorMsg);
                    } else {
                        showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR)); //todo error handler
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                showErrorMsg(getString(R.string.server_error));
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
