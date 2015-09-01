package com.bigbasket.mobileapp.activity.order;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.account.OTPValidationDialogFragment;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.InstantAutoCompleteTextView;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressFormActivity extends BackButtonActivity implements OtpDialogAware,
        CityListDisplayAware {

    private Address mAddress;
    private View base;
    private City mChoosenCity;
    private AutoCompleteTextView editTextPincode;
    private InstantAutoCompleteTextView editTextArea;
    private String mErrorMsg;
    private OTPValidationDialogFragment otpValidationDialogFragment;
    private boolean mFromAccountPage = false;
    private ArrayList<City> mCities;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getActivityTitle());
        mFromAccountPage = getIntent().getBooleanExtra(Constants.FROM_ACCOUNT_PAGE, false);
        mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        if (mAddress != null) {
            mChoosenCity = new City(mAddress.getCityName(), mAddress.getCityId());
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String cityName = preferences.getString(Constants.CITY, null);
            int cityId = Integer.parseInt(preferences.getString(Constants.CITY_ID, "1"));
            mChoosenCity = new City(cityName, cityId);
        }
        new GetCitiesTask<>(this).startTask();  // Sync the cities
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        // Callback once the cities get synced
        showForm(cities);
    }

    private void showForm(ArrayList<City> cities) {
        FrameLayout contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        contentLayout.setBackgroundColor(getResources().getColor(R.color.white));
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = inflater.inflate(R.layout.uiv3_member_address_form, contentLayout, false);
        editTextArea = (InstantAutoCompleteTextView) base.findViewById(R.id.editTextArea);
        editTextPincode = (AutoCompleteTextView) base.findViewById(R.id.editTextPincode);
        if (base == null) {
            finish();
            return;
        }
        Spinner citySpinner = (Spinner) base.findViewById(R.id.spinnerCity);

        int color = getResources().getColor(R.color.uiv3_primary_text_color);
        mCities = cities;
        BBArrayAdapter<City> arrayAdapter = new BBArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mCities, faceRobotoRegular,
                color, color);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(arrayAdapter);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChoosenCity = mCities.get(position);
                setAdapterArea(editTextArea, editTextPincode, mChoosenCity.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int selectedPosition = 0;
        for (int i = 0; i < mCities.size(); i++) {
            if (mCities.get(i).getId() == mChoosenCity.getId()) {
                mChoosenCity = mCities.get(i);
                selectedPosition = i;
                break;
            }
        }
        citySpinner.setSelection(selectedPosition);
        citySpinner.setEnabled(AuthParameters.getInstance(this).isMultiCityEnabled());

        TextView txtSaveAddress = (TextView) base.findViewById(R.id.txtSaveAddress);
        txtSaveAddress.setTypeface(faceRobotoMedium);
        txtSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAddress(null);
            }
        });
        if (mAddress != null) {
            populateUiFields();
        }
        setAdapterArea(editTextArea, editTextPincode, mChoosenCity.getName());
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
        CheckBox chkIsAddrDefault = (CheckBox) base.findViewById(R.id.chkIsAddrDefault);
        Spinner spinnerCity = (Spinner) base.findViewById(R.id.spinnerCity);

        if (!mAddress.getCityName().equals(mChoosenCity.getName())) {
            // Update the spinner
            int selectedPosition = 0;
            for (int i = 0; i < mCities.size(); i++) {
                if (mCities.get(i).getName().equals(mAddress.getCityName())) {
                    mChoosenCity = mCities.get(i);
                    selectedPosition = 0;
                }
            }
            spinnerCity.setSelection(selectedPosition);
        }
        spinnerCity.setEnabled(false); // Disallow user to change city for an existing address

        editTextAddressNick.setText(getValueOrBlank(mAddress.getAddressNickName()));
        editTextFirstName.setText(getValueOrBlank(mAddress.getFirstName()));
        editTextLastName.setText(getValueOrBlank(mAddress.getLastName()));
        editTextMobileNumber.setText(getValueOrBlank(mAddress.getContactNum()));
        editTextHouseNum.setText(getValueOrBlank(mAddress.getHouseNumber()));
        editTextStreetName.setText(getValueOrBlank(mAddress.getStreet()));
        editTextResidentialComplex.setText(getValueOrBlank(mAddress.getResidentialComplex()));
        editTextLandmark.setText(getValueOrBlank(mAddress.getLandmark()));
        editTextArea.setText(getValueOrBlank(mAddress.getArea()));

        //editTextCity.setText(getValueOrBlank(mAddress.getCityName()));
        editTextPincode.setText(getValueOrBlank(mAddress.getPincode()));
        //chkIsAddrDefault.setChecked(mAddress.isDefault()); // Don't remove since during request, this is read
        chkIsAddrDefault.setVisibility(mAddress.isDefault() ? View.GONE : View.VISIBLE);
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

        TextInputLayout textInputFirstName = (TextInputLayout) base.findViewById(R.id.textInputFirstName);
        TextInputLayout textInputLastName = (TextInputLayout) base.findViewById(R.id.textInputLastName);
        TextInputLayout textInputMobileNumber = (TextInputLayout) base.findViewById(R.id.textInputMobileNumber);
        TextInputLayout textInputHouseNum = (TextInputLayout) base.findViewById(R.id.textInputHouseNum);
        TextInputLayout textInputArea = (TextInputLayout) base.findViewById(R.id.textInputArea);
        TextInputLayout textInputPincode = (TextInputLayout) base.findViewById(R.id.textInputPincode);

        UIUtil.resetFormInputField(textInputFirstName);
        UIUtil.resetFormInputField(textInputLastName);
        UIUtil.resetFormInputField(textInputMobileNumber);
        UIUtil.resetFormInputField(textInputHouseNum);
        UIUtil.resetFormInputField(textInputArea);
        UIUtil.resetFormInputField(textInputPincode);

        // Validation
        boolean cancel = false;
        View focusView = null;
        if (isEditTextEmpty(editTextFirstName)) {
            UIUtil.reportFormInputFieldError(textInputFirstName, getString(R.string.error_field_required));
            focusView = editTextFirstName;
            cancel = true;
        }

        if (!UIUtil.isAlphaString(editTextFirstName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextFirstName;
            UIUtil.reportFormInputFieldError(textInputFirstName, getString(R.string.error_field_name));
        }

        if (isEditTextEmpty(editTextLastName)) {
            UIUtil.reportFormInputFieldError(textInputLastName, getString(R.string.error_field_required));
            if (focusView == null) focusView = editTextLastName;
            cancel = true;
        }

        if (!UIUtil.isAlphaString(editTextLastName.getText().toString().trim())) {
            cancel = true;
            if (focusView == null) focusView = editTextLastName;
            UIUtil.reportFormInputFieldError(textInputLastName, getString(R.string.error_field_name));
        }

        if (isEditTextEmpty(editTextMobileNumber)) {
            UIUtil.reportFormInputFieldError(textInputMobileNumber, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextMobileNumber;
            cancel = true;
        } else if (editTextMobileNumber.getText().toString().length() < 10) {
            UIUtil.reportFormInputFieldError(textInputMobileNumber, getString(R.string.contactNoMin10));
            if (focusView == null)
                focusView = editTextMobileNumber;
            cancel = true;
        }
        if (isEditTextEmpty(editTextHouseNum)) {
            UIUtil.reportFormInputFieldError(textInputHouseNum, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextHouseNum;
            cancel = true;
        }
        if (isEditTextEmpty(editTextArea)) {
            UIUtil.reportFormInputFieldError(textInputArea, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextArea;
            cancel = true;
        }
        if (isEditTextEmpty(editTextPincode)) {
            UIUtil.reportFormInputFieldError(textInputPincode, getString(R.string.error_field_required));
            if (focusView == null)
                focusView = editTextPincode;
            cancel = true;
        }

        if (editTextPincode.getText().length() < 6) {
            UIUtil.reportFormInputFieldError(textInputPincode, getString(R.string.pin_code_error));
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
        if (AuthParameters.getInstance(this).isMultiCityEnabled()) {
            payload.put(Constants.CITY_ID, String.valueOf(mChoosenCity.getId()));
        }

        if (otpCode != null) {
            payload.put(Constants.OTP_CODE, otpCode);
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, chkIsAddrDefault.isChecked() ? TrackEventkeys.YES : TrackEventkeys.NO);
        trackEvent(TrackingAware.ENABLE_DEFAULT_ADDRESS, eventAttribs);
        if (mAddress != null) {
            payload.put(Constants.ID, mAddress.getId());
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.updateAddress(payload, new CreateUpdateAddressApiCallback());
        } else {
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.createAddress(payload, new CreateUpdateAddressApiCallback());
        }
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
        if (mAddress == null) {
            mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        }
        return mAddress == null ? "Add Address" : "Update Address";
    }

    private void validateMobileNumber(boolean txtErrorValidateNumberVisibility, String errorMsg) {
        if (otpValidationDialogFragment == null) {
            otpValidationDialogFragment = OTPValidationDialogFragment.newInstance();
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
            if (isSuspended() || getCurrentActivity() == null) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (createUpdateAddressApiResponse.status) {
                case 0:
                    if (otpValidationDialogFragment != null) {
                        if (getCurrentActivity() != null && otpValidationDialogFragment.getEditTextMobileCode() != null)
                            BaseActivity.hideKeyboard(getCurrentActivity(), otpValidationDialogFragment.getEditTextMobileCode());
                        if (otpValidationDialogFragment.isVisible())
                            otpValidationDialogFragment.dismiss();
                    }

                    if (!mFromAccountPage) {
                        trackEvent(TrackingAware.CHECKOUT_ADDRESS_CREATED, null);
                    }
                    if (mAddress == null) {
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
                    trackEvent(mAddress == null ? TrackingAware.NEW_ADDRESS_FAILED :
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