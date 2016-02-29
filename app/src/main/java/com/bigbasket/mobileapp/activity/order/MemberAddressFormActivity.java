package com.bigbasket.mobileapp.activity.order;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.AddressAutoCompleteAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.address.AreaSearchResult;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.OtpValidationHelper;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.HashMapParcelUtils;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.DelayAutoCompleteTextView;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;


public class MemberAddressFormActivity extends BackButtonActivity implements OtpDialogAware,
        CityListDisplayAware {

    @Nullable
    private Address mAddress;
    private City mChoosenCity;
    private String mErrorMsg;
    private int mAddressPageMode;
    private ArrayList<City> mCities;
    private CheckBox chkIsAddrDefault;
    private DelayAutoCompleteTextView editTextPincode;
    private DelayAutoCompleteTextView editTextArea;
    private DelayAutoCompleteTextView editTextResidentialComplex;
    private AddressAutoCompleteAdapter resComplexSearchAdapter;
    private AddressAutoCompleteAdapter areaSearchAdapter;
    private AddressAutoCompleteAdapter pincodeSearchAdapter;
    private EditText editTextAddressNick;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextMobileNumber;
    private EditText editTextHouseNum;
    private EditText editTextStreetName;
    private EditText editTextLandmark;
    private Spinner citySpinner;
    private AreaSearchResult resComplexSearchResult;

    //MIN CHAR count for autocomplete
    private static final int THRESHOLD = 2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getActivityTitle());
        mAddressPageMode = getIntent().getIntExtra(Constants.ADDRESS_PAGE_MODE,
                MemberAddressPageMode.CHECKOUT);
        mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        if (mAddress != null) {
            mChoosenCity = new City(mAddress.getCityName(), mAddress.getCityId());
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String cityName = preferences.getString(Constants.CITY, null);
            int cityId = Integer.parseInt(preferences.getString(Constants.CITY_ID, "1"));
            mChoosenCity = new City(cityName, cityId);
        }
        if (savedInstanceState != null) {
            resComplexSearchResult = savedInstanceState.getParcelable(Constants.AREA_SEARCH_FIELD_LOCATION);
        }
        new GetCitiesTask<>(this).startTask();  // Sync the cities
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_member_address_form;
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        // Callback once the cities get synced
        showForm(cities);
    }

    private class CitySelectionListener implements AdapterView.OnItemSelectedListener {
        private boolean isFirstTime = true;
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mChoosenCity = mCities.get(position);
            int cityId = mChoosenCity.getId();
            resComplexSearchAdapter.setCityId(cityId);
            areaSearchAdapter.setCityId(cityId);
            pincodeSearchAdapter.setCityId(cityId);
            if(!isFirstTime) {
                //clear the auto update fields
                editTextResidentialComplex.setText("", false);
                editTextArea.setText("", false);
                editTextPincode.setText("", false);
            } else {
                isFirstTime = false ;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        public void setFirstTime(boolean firstTime) {
            isFirstTime = firstTime;
        }
    }

    private void showForm(ArrayList<City> cities) {
        editTextArea = (DelayAutoCompleteTextView) findViewById(R.id.editTextArea);
        editTextPincode = (DelayAutoCompleteTextView) findViewById(R.id.editTextPincode);
        editTextResidentialComplex = (DelayAutoCompleteTextView) findViewById(R.id.editTextResidentialComplex);
        editTextAddressNick = (EditText) findViewById(R.id.editTextAddressNick);
        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextFirstName.setNextFocusDownId(R.id.editTextLastName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextMobileNumber = (EditText) findViewById(R.id.editTextMobileNumber);
        editTextHouseNum = (EditText) findViewById(R.id.editTextHouseNum);
        editTextStreetName = (EditText) findViewById(R.id.editTextStreetName);
        editTextLandmark = (EditText) findViewById(R.id.editTextLandmark);
        chkIsAddrDefault = (CheckBox) findViewById(R.id.chkIsAddrDefault);
        citySpinner = (Spinner) findViewById(R.id.spinnerCity);

        int color = ContextCompat.getColor(this, R.color.uiv3_primary_text_color);
        mCities = cities;
        BBArrayAdapter<City> arrayAdapter = new BBArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mCities, faceRobotoRegular,
                color, color);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(arrayAdapter);

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
        CitySelectionListener selectionListener = new CitySelectionListener();
        if(mAddress == null) {
            selectionListener.setFirstTime(false);
        }
        citySpinner.setOnItemSelectedListener(selectionListener);

        Button txtSaveAddress = (Button) findViewById(R.id.txtSaveAddress);
        txtSaveAddress.setTypeface(faceRobotoMedium);
        txtSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAddress(null, false);
            }
        });
        if (mAddress != null) {
            populateUiFields();
        }

        TextView lblNeedMoreAddressInfo = (TextView) findViewById(R.id.lblNeedMoreAddressInfo);
        if (mAddress != null && mAddress.isPartial() && mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
            lblNeedMoreAddressInfo.setTypeface(faceRobotoRegular);
            lblNeedMoreAddressInfo.setVisibility(View.VISIBLE);
        } else {
            lblNeedMoreAddressInfo.setVisibility(View.GONE);
        }

        resComplexSearchAdapter = new AddressAutoCompleteAdapter(this, mChoosenCity.getId(),
                Constants.AREA_SEARCH_TYPE_APARTMENT);
        resComplexSearchAdapter.setResultFields(Constants.AREA_SEARCH_FIELD_DISPLAY_NAME,
                Constants.AREA_SEARCH_FIELD_AREA,
                Constants.AREA_SEARCH_FIELD_PINCODE,
                Constants.AREA_SEARCH_FIELD_STREET,
                Constants.AREA_SEARCH_FIELD_LANDMARK,
                Constants.AREA_SEARCH_FIELD_LOCATION);
        areaSearchAdapter = new AddressAutoCompleteAdapter(this, mChoosenCity.getId(),
                Constants.AREA_SEARCH_TYPE_AREA);
        areaSearchAdapter.setResultFields(Constants.AREA_SEARCH_FIELD_DISPLAY_NAME,
                Constants.AREA_SEARCH_FIELD_PINCODE,
                Constants.AREA_SEARCH_FIELD_STREET,
                Constants.AREA_SEARCH_FIELD_LANDMARK);
        pincodeSearchAdapter = new AddressAutoCompleteAdapter(this, mChoosenCity.getId(),
                Constants.AREA_SEARCH_TYPE_PINCODE);
        pincodeSearchAdapter.setResultFields(Constants.AREA_SEARCH_FIELD_DISPLAY_NAME,
                Constants.AREA_SEARCH_FIELD_STREET,
                Constants.AREA_SEARCH_FIELD_LANDMARK);

        editTextResidentialComplex.setLoadingIndicator(
                (ProgressBar) findViewById(R.id.res_complex_loading_indicator));
        editTextResidentialComplex.setThreshold(THRESHOLD);
        editTextResidentialComplex.setAdapter(resComplexSearchAdapter);

        editTextArea.setLoadingIndicator(
                (ProgressBar) findViewById(R.id.area_loading_indicator));
        editTextArea.setThreshold(THRESHOLD);
        editTextArea.setAdapter(areaSearchAdapter);

        editTextPincode.setLoadingIndicator(
                (ProgressBar) findViewById(R.id.pincode_loading_indicator));
        editTextPincode.setThreshold(THRESHOLD);
        editTextPincode.setAdapter(pincodeSearchAdapter);

        editTextResidentialComplex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AreaSearchResult searchResult = (AreaSearchResult) resComplexSearchAdapter.getItem(position);
                if(searchResult != null) {
                    editTextResidentialComplex.setText(searchResult.getDisplayName(), false);
                    if(!TextUtils.isEmpty(searchResult.getArea())){
                        editTextArea.setText(searchResult.getArea(), false);
                    }
                    if(!TextUtils.isEmpty(searchResult.getPincode())){
                        editTextPincode.setText(searchResult.getPincode(), false);
                    }
                    if(!TextUtils.isEmpty(searchResult.getLandmark())){
                        editTextLandmark.setText(searchResult.getLandmark());
                    }
                    if(!TextUtils.isEmpty(searchResult.getStreet())){
                        editTextStreetName.setText(searchResult.getStreet());
                    }
                    resComplexSearchResult = searchResult;
                }
            }
        });

        editTextArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AreaSearchResult searchResult = (AreaSearchResult)areaSearchAdapter.getItem(position);
                if(searchResult != null) {
                    editTextArea.setText(searchResult.getDisplayName(), false);
                    if(!TextUtils.isEmpty(searchResult.getPincode())){
                        editTextPincode.setText(searchResult.getPincode(), false);
                    }
                    if(!TextUtils.isEmpty(searchResult.getLandmark())){
                        editTextLandmark.setText(searchResult.getLandmark());
                    }
                    if(!TextUtils.isEmpty(searchResult.getStreet())){
                        editTextStreetName.setText(searchResult.getStreet());
                    }
                }
            }
        });

        editTextPincode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AreaSearchResult searchResult = (AreaSearchResult)pincodeSearchAdapter.getItem(position);
                if(searchResult != null) {
                    if(!TextUtils.isEmpty(searchResult.getDisplayName())) {
                        editTextPincode.setText(searchResult.getDisplayName(), false);
                    } else {
                        editTextPincode.setText(searchResult.getPincode(), false);
                    }
                    if(!TextUtils.isEmpty(searchResult.getLandmark())){
                        editTextLandmark.setText(searchResult.getLandmark());
                    }
                    if(!TextUtils.isEmpty(searchResult.getStreet())){
                        editTextStreetName.setText(searchResult.getStreet());
                    }
                }
            }
        });

    }

    private void populateUiFields() {
        if (mAddress == null) return;

        if (!mAddress.getCityName().equals(mChoosenCity.getName())) {
            // Update the spinner
            int selectedPosition = 0;
            for (int i = 0; i < mCities.size(); i++) {
                if (mCities.get(i).getName().equals(mAddress.getCityName())) {
                    mChoosenCity = mCities.get(i);
                    selectedPosition = 0;
                }
            }
            citySpinner.setSelection(selectedPosition);
        }

        editTextAddressNick.setText(getValueOrBlank(mAddress.getAddressNickName()));
        editTextFirstName.setText(getValueOrBlank(mAddress.getFirstName()));
        editTextLastName.setText(getValueOrBlank(mAddress.getLastName()));
        editTextMobileNumber.setText(getValueOrBlank(mAddress.getContactNum()));
        editTextHouseNum.setText(getValueOrBlank(mAddress.getHouseNumber()));
        editTextStreetName.setText(getValueOrBlank(mAddress.getStreet()));
        editTextResidentialComplex.setText(getValueOrBlank(mAddress.getResidentialComplex()), false);
        editTextLandmark.setText(getValueOrBlank(mAddress.getLandmark()));
        editTextArea.setText(getValueOrBlank(mAddress.getArea()), false);
        editTextPincode.setText(getValueOrBlank(mAddress.getPincode()), false);
        chkIsAddrDefault.setVisibility(mAddress.isDefault() ? View.GONE : View.VISIBLE);
    }

    private void uploadAddress(String otpCode, boolean isResendOtpRequested) {
        if (chkIsAddrDefault == null) {
            chkIsAddrDefault = (CheckBox) findViewById(R.id.chkIsAddrDefault);
        }

        TextInputLayout textInputFirstName = (TextInputLayout) findViewById(R.id.textInputFirstName);
        TextInputLayout textInputLastName = (TextInputLayout) findViewById(R.id.textInputLastName);
        TextInputLayout textInputMobileNumber = (TextInputLayout) findViewById(R.id.textInputMobileNumber);
        TextInputLayout textInputHouseNum = (TextInputLayout) findViewById(R.id.textInputHouseNum);
        TextInputLayout textInputArea = (TextInputLayout) findViewById(R.id.textInputArea);
        TextInputLayout textInputPincode = (TextInputLayout) findViewById(R.id.textInputPincode);

        UIUtil.resetFormInputField(textInputFirstName);
        UIUtil.resetFormInputField(textInputLastName);
        UIUtil.resetFormInputField(textInputMobileNumber);
        UIUtil.resetFormInputField(textInputHouseNum);
        UIUtil.resetFormInputField(textInputArea);
        UIUtil.resetFormInputField(textInputPincode);

        hideKeyboard(this, editTextAddressNick);

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
        if (editTextArea != null && isEditTextEmpty(editTextArea)) {
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

        final HashMap<String, String> payload = new HashMap<>();
        payload.put(Constants.ADDR_NICK, editTextAddressNick.getText().toString());
        payload.put(Constants.FIRSTNAME, editTextFirstName.getText().toString());
        payload.put(Constants.LASTNAME, editTextLastName.getText().toString());
        payload.put(Constants.AREA, editTextArea.getText().toString());
        payload.put(Constants.CONTACT_NUM, editTextMobileNumber.getText().toString());
        payload.put(Constants.STREET, editTextStreetName.getText().toString());
        payload.put(Constants.HOUSE_NO, editTextHouseNum.getText().toString());
        payload.put(Constants.PIN, editTextPincode.getText().toString());
        payload.put(Constants.LANDMARK, editTextLandmark.getText().toString());
        payload.put(Constants.RES_CMPLX, editTextResidentialComplex.getText().toString());
        boolean isDefault = (mAddress != null && mAddress.isDefault()) || chkIsAddrDefault.isChecked();
        payload.put(Constants.IS_DEFAULT, isDefault ? "1" : "0");

        AreaSearchResult complexDetails = resComplexSearchResult;

        if(complexDetails == null && mAddress != null) {
            complexDetails = new AreaSearchResult();
            complexDetails.setDisplayName(mAddress.getResidentialComplex());
            complexDetails.setArea(mAddress.getArea());
            complexDetails.setPincode(mAddress.getPincode());
            complexDetails.setLocation(mAddress.getLocation());
        }

        //Set the location(GeoCode) only if complex name, area and pincode match
        if(complexDetails != null && complexDetails.getLocation() != null) {
            String resComplex = payload.get(Constants.RES_CMPLX);
            String area = payload.get(Constants.AREA);
            String pin = payload.get(Constants.PIN);
            if(resComplex != null && resComplex.equals(complexDetails.getDisplayName())){
                if(complexDetails.getArea() != null
                        && area.equalsIgnoreCase(complexDetails.getArea())) {
                    if(complexDetails.getPincode() != null
                            && pin.equalsIgnoreCase(complexDetails.getPincode())) {
                        payload.put(Constants.LOCATION,
                                TextUtils.join(",", complexDetails.getLocation()));
                    }
                }
            }
        }
        if (AuthParameters.getInstance(this).isMultiCityEnabled()) {
            payload.put(Constants.CITY_ID, String.valueOf(mChoosenCity.getId()));
        }

        if (otpCode != null) {
            payload.put(Constants.OTP_CODE, otpCode);
        }

        if (mAddress != null && mChoosenCity.getId() != mAddress.getCityId()) {
            if (TextUtils.isEmpty(otpCode)) {
                Bundle data = HashMapParcelUtils.mapToBundle(payload);
                // User is trying to change the city, show an alert
                showAlertDialog(getString(R.string.createNewAddress),
                        getString(R.string.newAddressNotAllowed),
                        DialogButton.OK, DialogButton.CANCEL,
                        Constants.UPDATE_ADDRESS_DIALOG_REQUEST,
                        data, getString(R.string.lblContinue));
            } else {
                uploadAddress(payload, true, isResendOtpRequested);
            }
        } else {
            uploadAddress(payload, false, isResendOtpRequested);
        }
    }

    private void uploadAddress(HashMap<String, String> payload, boolean forceCreate,
                               boolean isResendOtpRequested) {
        if (chkIsAddrDefault == null) {
            chkIsAddrDefault = (CheckBox) findViewById(R.id.chkIsAddrDefault);
        }
        if (chkIsAddrDefault.getVisibility() == View.VISIBLE
                && ((mAddress != null && chkIsAddrDefault.isChecked() != mAddress.isDefault())
                || (mAddress == null && chkIsAddrDefault.isChecked()))) {
            HashMap<String, String> eventAttribs = new HashMap<>();
            trackEvent(TrackingAware.ENABLE_DEFAULT_ADDRESS, eventAttribs);
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        if (mAddress != null && !forceCreate) {
            if (!TextUtils.isEmpty(mAddress.getId())) {
                payload.put(Constants.ID, mAddress.getId());
                showProgressDialog(isResendOtpRequested ? getString(R.string.resending_otp) :
                        getString(R.string.please_wait));
                Call<ApiResponse<CreateUpdateAddressApiResponseContent>> call =
                        bigBasketApiService.updateAddress(getPreviousScreenName(), payload);
                call.enqueue(new CreateUpdateAddressApiCallback(this, false, isResendOtpRequested));
            } else {
                //handling if the address is created manually without id
                showProgressDialog(isResendOtpRequested ? getString(R.string.resending_otp) :
                        getString(R.string.please_wait));
                Call<ApiResponse<CreateUpdateAddressApiResponseContent>> call =
                        bigBasketApiService.createAddress(getPreviousScreenName(), payload);
                call.enqueue(new CreateUpdateAddressApiCallback(this, false, isResendOtpRequested));
            }
        } else {
            payload.remove(Constants.ID); // Defensive check
            showProgressDialog(isResendOtpRequested ? getString(R.string.resending_otp) :
                    getString(R.string.please_wait));
            Call<ApiResponse<CreateUpdateAddressApiResponseContent>> call =
                    bigBasketApiService.createAddress(getPreviousScreenName(), payload);
            call.enqueue(new CreateUpdateAddressApiCallback(this, forceCreate, isResendOtpRequested));
        }
    }

    @Override
    public void validateOtp(String otpCode, boolean isResendOtpRequested) {
        uploadAddress(otpCode, isResendOtpRequested);
    }

    private void addressCreatedModified(Address address) {
        AppDataDynamic.reset(this);
        Intent result = new Intent();
        result.putExtra(Constants.UPDATE_ADDRESS, address);
        setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED, result);
        finish();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText() == null || TextUtils.isEmpty(editText.getText().toString().trim());
    }

    private String getActivityTitle() {
        if (mAddress == null) {
            mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        }
        return mAddress == null ? getString(R.string.addAddress) : getString(R.string.updateAddress);
    }

    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case Constants.UPDATE_ADDRESS_DIALOG_REQUEST:
                if (valuePassed != null) {
                    HashMap<String, String> payLoad = HashMapParcelUtils.bundleToMap(valuePassed);
                    uploadAddress(payLoad, true, false);
                }
                break;
            default:
                super.onPositiveButtonClicked(sourceName, valuePassed);
        }

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
                OtpValidationHelper.requestOtpUI(this);
                break;
            case Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG:
                OtpValidationHelper.reportError(this, mErrorMsg);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(resComplexSearchResult != null) {
            outState.putParcelable(Constants.AREA_SEARCH_FIELD_LOCATION, resComplexSearchResult);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean handled = OtpValidationHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        if (!handled) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNoFragmentsInLayout() {
        OtpValidationHelper.onDestroy();
        // No need to finish activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OtpValidationHelper.onDestroy();
    }

    private class CreateUpdateAddressApiCallback extends BBNetworkCallback<ApiResponse<CreateUpdateAddressApiResponseContent>> {

        private boolean forceCreate;
        private boolean isResendOtpRequested;

        public CreateUpdateAddressApiCallback(AppOperationAware ctx, boolean forceCreate, boolean isResendOtpRequested) {
            super(ctx);
            this.forceCreate = forceCreate;
            this.isResendOtpRequested = isResendOtpRequested;
        }

        @Override
        public void onSuccess(ApiResponse<CreateUpdateAddressApiResponseContent> createUpdateAddressApiResponse) {
            switch (createUpdateAddressApiResponse.status) {
                case 0:
                    OtpValidationHelper.dismiss();
                    if (mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
                        trackEvent(TrackingAware.CHECKOUT_ADDRESS_CREATED, null);
                    }
                    if (mAddress == null || forceCreate) {
                        Toast.makeText(getCurrentActivity(), R.string.addressAdded, Toast.LENGTH_LONG).show();
                        addressCreatedModified(createUpdateAddressApiResponse.apiResponseContent.address);
                    } else {
                        Toast.makeText(getCurrentActivity(), R.string.addressUpdated, Toast.LENGTH_LONG).show();
                        addressCreatedModified(createUpdateAddressApiResponse.apiResponseContent.address);
                    }
                    break;
                case ApiErrorCodes.NUMBER_IN_USE:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    handleMessage(Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER);
                    break;
                case ApiErrorCodes.OTP_NEEDED:
                    if (isResendOtpRequested) {
                        showToast(getString(R.string.resendOtpMsg));
                    }
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
        public boolean updateProgress() {
            try {
                hideProgressDialog();
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}