package com.bigbasket.mobileapp.activity.order;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.account.OTPValidationDialogFragment;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressFormActivity extends BackButtonActivity implements PinCodeAware, OtpDialogAware {

    private Address address;
    private View base;
    private String cityName;
    private AutoCompleteTextView editTextPincode;
    private AutoCompleteTextView editTextArea;
    private String mErrorMsg;
    private OTPValidationDialogFragment otpValidationDialogFragment;
    private boolean mFromAccountPage = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getActivityTitle());
        mFromAccountPage = getIntent().getBooleanExtra(Constants.FROM_ACCOUNT_PAGE, false);
        if (address == null) {
            address = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            cityName = preferences.getString(Constants.CITY, null);
        }

        String addressCityName = address != null ? address.getCityName() : null;
        if (getSystemAreaInfo(addressCityName)) {
            handleMessage(Constants.CALL_AREA_INFO);
        } else {
            showForm();
        }
    }

    private void showForm() {
        FrameLayout contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        contentLayout.setBackgroundColor(getResources().getColor(R.color.white));
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = inflater.inflate(R.layout.uiv3_member_address_form, contentLayout, false);
        editTextArea = (AutoCompleteTextView) base.findViewById(R.id.editTextArea);
        editTextPincode = (AutoCompleteTextView) base.findViewById(R.id.editTextPincode);
        if (base == null) {
            finish();
            return;
        }
        TextView txtSaveAddress = (TextView) base.findViewById(R.id.txtSaveAddress);
        txtSaveAddress.setTypeface(faceRobotoMedium);
        txtSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAddress(null);
            }
        });
        if (address != null) {
            populateUiFields();
        } else {
            EditText editTextCity = (EditText) base.findViewById(R.id.editTextCity);
            editTextCity.setText(cityName);
        }
        setAdapterArea(editTextArea, editTextPincode);
        contentLayout.addView(base);
    }

    private void populateUiFields() {
        EditText editTextAddressNick = (EditText) base.findViewById(R.id.editTextAddressNick);
        EditText editTextFirstName = (EditText) base.findViewById(R.id.editTextFirstName);
        editTextFirstName.setNextFocusDownId(R.id.editTextLastName);
        EditText editTextLastName = (EditText) base.findViewById(R.id.editTextLastName);
        EditText editTextMobileNumber = (EditText) base.findViewById(R.id.editTextMobileNumber);
        EditText editTextHouseNum = (EditText) base.findViewById(R.id.editTextHouseNum);
        EditText editTextStreetName = (EditText) base.findViewById(R.id.editTextStreetName);
        EditText editTextResidentialComplex = (EditText) base.findViewById(R.id.editTextResidentialComplex);
        EditText editTextLandmark = (EditText) base.findViewById(R.id.editTextLandmark);
        EditText editTextCity = (EditText) base.findViewById(R.id.editTextCity);
        CheckBox chkIsAddrDefault = (CheckBox) base.findViewById(R.id.chkIsAddrDefault);

        editTextAddressNick.setText(getValueOrBlank(address.getAddressNickName()));
        editTextFirstName.setText(getValueOrBlank(address.getFirstName()));
        editTextLastName.setText(getValueOrBlank(address.getLastName()));
        editTextMobileNumber.setText(getValueOrBlank(address.getContactNum()));
        editTextHouseNum.setText(getValueOrBlank(address.getHouseNumber()));
        editTextStreetName.setText(getValueOrBlank(address.getStreet()));
        editTextResidentialComplex.setText(getValueOrBlank(address.getResidentialComplex()));
        editTextLandmark.setText(getValueOrBlank(address.getLandmark()));
        editTextArea.setText(getValueOrBlank(address.getArea()));
        editTextCity.setText(getValueOrBlank(address.getCityName()));
        editTextPincode.setText(getValueOrBlank(address.getPincode()));
        //chkIsAddrDefault.setChecked(address.isDefault()); // Don't remove since during request, this is read
        chkIsAddrDefault.setVisibility(address.isDefault() ? View.GONE : View.VISIBLE);
    }

    private void uploadAddress(String otpCode) {
        final EditText editTextAddressNick = (EditText) base.findViewById(R.id.editTextAddressNick);
        final EditText editTextFirstName = (EditText) base.findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = (EditText) base.findViewById(R.id.editTextLastName);
        final EditText editTextMobileNumber = (EditText) base.findViewById(R.id.editTextMobileNumber);
        final EditText editTextHouseNum = (EditText) base.findViewById(R.id.editTextHouseNum);
        final EditText editTextStreetName = (EditText) base.findViewById(R.id.editTextStreetName);
        final EditText editTextResidentialComplex = (EditText)
                base.findViewById(R.id.editTextResidentialComplex);
        final EditText editTextLandmark = (EditText) base.findViewById(R.id.editTextLandmark);
        final CheckBox chkIsAddrDefault = (CheckBox) base.findViewById(R.id.chkIsAddrDefault);

        editTextAddressNick.setError(null);
        editTextFirstName.setError(null);
        editTextLastName.setError(null);
        editTextMobileNumber.setError(null);
        editTextHouseNum.setError(null);
        editTextStreetName.setError(null);
        editTextLandmark.setError(null);
        editTextArea.setError(null);
        editTextPincode.setError(null);

        // Validation
        boolean cancel = false;
        View focusView = null;
        if (isEditTextEmpty(editTextFirstName)) {
            reportFormInputFieldError(editTextFirstName, getString(R.string.error_field_required));
            focusView = editTextFirstName;
            cancel = true;
        }

        if (!UIUtil.isAlphaString(editTextFirstName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextFirstName;
            UIUtil.reportFormInputFieldError(editTextFirstName, getString(R.string.error_field_name));
        }

        if (isEditTextEmpty(editTextLastName)) {
            reportFormInputFieldError(editTextLastName, getString(R.string.error_field_required));
            if (focusView == null) focusView = editTextLastName;
            cancel = true;
        }

        if (!UIUtil.isAlphaString(editTextLastName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextLastName;
            UIUtil.reportFormInputFieldError(editTextLastName, getString(R.string.error_field_name));
        }

        if (isEditTextEmpty(editTextMobileNumber)) {
            reportFormInputFieldError(editTextMobileNumber, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextMobileNumber;
            cancel = true;
        } else if (editTextMobileNumber.getText().toString().length() < 10) {
            reportFormInputFieldError(editTextMobileNumber, getString(R.string.contactNoMin10));
            if (focusView == null)
                focusView = editTextMobileNumber;
            cancel = true;
        }
        if (isEditTextEmpty(editTextHouseNum)) {
            reportFormInputFieldError(editTextHouseNum, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextHouseNum;
            cancel = true;
        }
        if (isEditTextEmpty(editTextArea)) {
            reportFormInputFieldError(editTextArea, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextArea;
            cancel = true;
        }
        if (isEditTextEmpty(editTextPincode)) {
            reportFormInputFieldError(editTextPincode, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextPincode;
            cancel = true;
        }

        if (editTextPincode.getText().length() < 6) {
            reportFormInputFieldError(editTextPincode, getString(R.string.pin_code_error));
            if (focusView == null)
                focusView = editTextPincode;
            cancel = true;
        }

        if (cancel) {
            // There was an error, don't sign-up
            focusView.requestFocus();
            return;
        }

        // Sending request

        HashMap<String, String> payload = new HashMap<String, String>() {
            {
                put(Constants.ADDR_NICK, editTextAddressNick.getText().toString());
            }

            {
                put(Constants.FIRSTNAME, editTextFirstName.getText().toString());
            }

            {
                put(Constants.LASTNAME, editTextLastName.getText().toString());
            }

            {
                put(Constants.AREA, editTextArea.getText().toString());
            }

            {
                put(Constants.CONTACT_NUM, editTextMobileNumber.getText().toString());
            }

            {
                put(Constants.STREET, editTextStreetName.getText().toString());
            }

            {
                put(Constants.HOUSE_NO, editTextHouseNum.getText().toString());
            }

            {
                put(Constants.PIN, editTextPincode.getText().toString());
            }

            {
                put(Constants.LANDMARK, editTextLandmark.getText().toString());
            }

            {
                put(Constants.RES_CMPLX, editTextResidentialComplex.getText().toString());
            }

            {
                put(Constants.IS_DEFAULT, chkIsAddrDefault.isChecked() ? "1" : "0");
            }
        };

        if (otpCode != null) {
            payload.put(Constants.OTP_CODE, otpCode);
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, chkIsAddrDefault.isChecked() ? TrackEventkeys.YES : TrackEventkeys.NO);
        trackEvent(TrackingAware.ENABLE_DEFAULT_ADDRESS, eventAttribs);
        if (address != null) {
            payload.put(Constants.ID, address.getId());
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.updateAddress(payload, new CreateUpdateAddressApiCallback());
        } else {
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.createAddress(payload, new CreateUpdateAddressApiCallback());
        }
    }

    @Override
    public void onPinCodeFetchSuccess() {
        showForm();
        setAdapterArea(editTextArea, editTextPincode);
    }

    @Override
    public void onPinCodeFetchFailure() {
        showForm();
    }

    @Override
    public void validateOtp(String otpCode) {
        uploadAddress(otpCode);
    }

    private void addressCreatedModified(String addressId) {
        Intent result = new Intent();
        result.putExtra(Constants.MEMBER_ADDRESS_ID, addressId);
        setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED, result);
        finish();
    }

    private void addressCreatedModified() {
        setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED);
        finish();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    public String getActivityTitle() {
        if (address == null) {
            address = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        }
        return address == null ? "Add address" : "Update address";
    }

    private void validateMobileNumber(boolean txtErrorValidateNumberVisibility, String errorMsg) {
        if (otpValidationDialogFragment == null) {
            otpValidationDialogFragment = OTPValidationDialogFragment.newInstance(false);
        }
        if (otpValidationDialogFragment.isVisible()) {
            if (txtErrorValidateNumberVisibility) {
                otpValidationDialogFragment.showErrorText(errorMsg);
            }
        } else {
            otpValidationDialogFragment.show(getCurrentActivity().getSupportFragmentManager(),
                    Constants.OTP_DIALOG_FLAG);
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null && sourceName.equalsIgnoreCase(Constants.ERROR)) {
            finish();
        }
        super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
    }

    @Override
    public void showProgressView() {
        showProgressDialog(getString(R.string.please_wait));
    }

    @Override
    public void hideProgressView() {
        hideProgressDialog();
    }

    public void handleMessage(int what) {
        switch (what) {
            case Constants.CALL_AREA_INFO:
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
                showProgressDialog(getString(R.string.please_wait));
                bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(getCurrentActivity()));
                break;
            case Constants.VALIDATE_MOBILE_NUMBER_POPUP:
                validateMobileNumber(false, mErrorMsg);
                break;
            case Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG:
                validateMobileNumber(true, mErrorMsg);
                break;
            case Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER:
                showAlertDialog(mErrorMsg != null ? mErrorMsg : getResources().getString(R.string.numberUsedByAnotherMember));
                break;
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.CREATE_OR_EDIT_DELIVERY_ADDRESS_SCREEN;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (editTextPincode != null) {
            hideKeyboard(this, editTextPincode);
        }
    }

    class CreateUpdateAddressApiCallback implements Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> {

        @Override
        public void success(ApiResponse<CreateUpdateAddressApiResponseContent> createUpdateAddressApiResponse, Response response) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (createUpdateAddressApiResponse.status) {
                case 0:
                    if (otpValidationDialogFragment != null && otpValidationDialogFragment.isVisible()) {
                        if (getCurrentActivity() != null)
                            BaseActivity.hideKeyboard(getCurrentActivity(), getCurrentActivity().getCurrentFocus());
                        otpValidationDialogFragment.dismiss();
                    }

                    if (!mFromAccountPage) {
                        HashMap<String, String> map = new HashMap<>();
                        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                        map.put(TrackEventkeys.POTENTIAL_ORDER, prefer.getString(Constants.POTENTIAL_ORDER_ID, null));
                        trackEvent(TrackingAware.CHECKOUT_ADDRESS_CREATED, map);
                    }
                    if (address == null) {
                        Toast.makeText(getCurrentActivity(), "Address added successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified(createUpdateAddressApiResponse.apiResponseContent.addressId);
                    } else {
                        Toast.makeText(getCurrentActivity(), "Address updated successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified();
                    }

                    break;
                case ApiErrorCodes.NUMBER_IN_USE:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    handleMessage(Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER);
                    break;
                case ApiErrorCodes.OTP_NEEDED:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    handleMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP);
                    break;
                case ApiErrorCodes.OTP_INVALID:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    handleMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG);
                    break;
                default:
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.FAILURE_REASON, createUpdateAddressApiResponse.message);
                    map.put(TrackEventkeys.NAVIGATION_CTX, mFromAccountPage ? TrackEventkeys.NAVIGATION_CTX_MY_ACCOUNT :
                            TrackEventkeys.NAVIGATION_CTX_MY_ACCOUNT);
                    trackEvent(address == null ? TrackingAware.NEW_ADDRESS_FAILED :
                            TrackingAware.UPDATE_ADDRESS_FAILED, map);
                    handler.sendEmptyMessage(createUpdateAddressApiResponse.status,
                            createUpdateAddressApiResponse.message, false);
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            handler.handleRetrofitError(error);
        }
    }
}