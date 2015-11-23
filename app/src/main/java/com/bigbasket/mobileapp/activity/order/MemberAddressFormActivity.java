package com.bigbasket.mobileapp.activity.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.BBUrlEncodeUtils;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.InstantAutoCompleteTextView;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Call;


public class MemberAddressFormActivity extends BackButtonActivity implements OtpDialogAware,
        CityListDisplayAware {

    private Address mAddress;
    private View base;
    private City mChoosenCity;
    private AutoCompleteTextView editTextPincode;
    private InstantAutoCompleteTextView editTextArea;
    private String mErrorMsg;
    private OTPValidationDialogFragment otpValidationDialogFragment;
    private int mAddressPageMode;
    private ArrayList<City> mCities;
    @Nullable
    private BroadcastReceiver broadcastReceiver;
    private HashMap<String, String> mPayload;

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

        Button txtSaveAddress = (Button) base.findViewById(R.id.txtSaveAddress);
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

        TextView lblNeedMoreAddressInfo = (TextView) base.findViewById(R.id.lblNeedMoreAddressInfo);
        if (mAddress != null && mAddress.isPartial() && mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
            lblNeedMoreAddressInfo.setTypeface(faceRobotoRegular);
            lblNeedMoreAddressInfo.setVisibility(View.VISIBLE);
        } else {
            lblNeedMoreAddressInfo.setVisibility(View.GONE);
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

        editTextAddressNick.setText(getValueOrBlank(mAddress.getAddressNickName()));
        editTextFirstName.setText(getValueOrBlank(mAddress.getFirstName()));
        editTextLastName.setText(getValueOrBlank(mAddress.getLastName()));
        editTextMobileNumber.setText(getValueOrBlank(mAddress.getContactNum()));
        editTextHouseNum.setText(getValueOrBlank(mAddress.getHouseNumber()));
        editTextStreetName.setText(getValueOrBlank(mAddress.getStreet()));
        editTextResidentialComplex.setText(getValueOrBlank(mAddress.getResidentialComplex()));
        editTextLandmark.setText(getValueOrBlank(mAddress.getLandmark()));
        editTextArea.setText(getValueOrBlank(mAddress.getArea()));
        editTextPincode.setText(getValueOrBlank(mAddress.getPincode()));
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
        payload.put(Constants.IS_DEFAULT, chkIsAddrDefault.isChecked() ? "1" : "0");

        if (AuthParameters.getInstance(this).isMultiCityEnabled()) {
            payload.put(Constants.CITY_ID, String.valueOf(mChoosenCity.getId()));
        }

        if (otpCode != null) {
            payload.put(Constants.OTP_CODE, otpCode);
        }

        if (mAddress != null && mChoosenCity.getId() != mAddress.getCityId()) {
            if (TextUtils.isEmpty(otpCode)) {
                // User is trying to change the city, show an alert
                mPayload = payload;
                showAlertDialog(getString(R.string.createNewAddress),
                        getString(R.string.newAddressNotAllowed),
                        DialogButton.OK, DialogButton.CANCEL,
                        Constants.UPDATE_ADDRESS_DIALOG_REQUEST,
                        null, getString(R.string.lblContinue));
            } else {
                uploadAddress(payload, true);
            }
        } else {
            uploadAddress(payload, false);
        }
    }

    private void uploadAddress(Map<String, String> payload, boolean forceCreate) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Map<String, String> eventAttribs = new HashMap<>();
        trackEvent(TrackingAware.ENABLE_DEFAULT_ADDRESS, eventAttribs);
        if (mAddress != null && !forceCreate) {
            payload.put(Constants.ID, mAddress.getId());
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<CreateUpdateAddressApiResponseContent>> call =
                    bigBasketApiService.updateAddress(BBUrlEncodeUtils.urlEncode(payload));
            call.enqueue(new CreateUpdateAddressApiCallback(this, false));
        } else {
            payload.remove(Constants.ID); // Defensive check
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<CreateUpdateAddressApiResponseContent>> call =
                    bigBasketApiService.createAddress(BBUrlEncodeUtils.urlEncode(payload));
            call.enqueue(new CreateUpdateAddressApiCallback(this, forceCreate));
        }
    }

    @Override
    public void validateOtp(String otpCode) {
        uploadAddress(otpCode);
    }

    private void addressCreatedModified(Address address) {
        Intent result = new Intent();
        result.putExtra(Constants.UPDATE_ADDRESS, address);
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

    private String getActivityTitle() {
        if (mAddress == null) {
            mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
        }
        return mAddress == null ? getString(R.string.addAddress) : getString(R.string.updateAddress);
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
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case Constants.UPDATE_ADDRESS_DIALOG_REQUEST:
                if(mPayload != null) {
                    uploadAddress(mPayload, true);
                    mPayload = null;
                }
                break;
            default:
                super.onPositiveButtonClicked( sourceName, valuePassed);
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
        /**
         * unregistering the sms broadcast receiver
         *
         */
        unregisterBroadcastForSMS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastForSMS();

    }

    private void registerBroadcastForSMS() {
        IntentFilter smsOTPintentFilter = new IntentFilter();
        smsOTPintentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsOTPintentFilter.setPriority(2147483647);//setting high priority for dual sim support
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    if (pdusObj == null) return;
                    for (Object aPduObj : pdusObj) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPduObj);
                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();

                        /**
                         * checking that the message received is from BigBasket
                         * and it contains the word verification
                         */
                        if ((phoneNumber.toUpperCase().contains("BIG") &&
                                (message.toLowerCase().contains("verification")))) {
                            final Pattern p = Pattern.compile("(\\d{4})");
                            final Matcher m = p.matcher(message);
                            if (m.find() && otpValidationDialogFragment != null
                                    && otpValidationDialogFragment.isVisible()) {
                                otpValidationDialogFragment.resendOrConfirmOTP(m.group(0));
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, smsOTPintentFilter);
    }

    private void unregisterBroadcastForSMS() {
        if (broadcastReceiver == null) return;
        unregisterReceiver(broadcastReceiver);
    }

    private class CreateUpdateAddressApiCallback extends BBNetworkCallback<ApiResponse<CreateUpdateAddressApiResponseContent>> {

        private boolean forceCreate;

        public CreateUpdateAddressApiCallback(AppOperationAware ctx, boolean forceCreate) {
            super(ctx);
            this.forceCreate = forceCreate;
        }

        @Override
        public void onSuccess(ApiResponse<CreateUpdateAddressApiResponseContent> createUpdateAddressApiResponse) {
            switch (createUpdateAddressApiResponse.status) {
                case 0:
                    if (otpValidationDialogFragment != null) {
                        if (getCurrentActivity() != null && otpValidationDialogFragment.getEditTextMobileCode() != null)
                            BaseActivity.hideKeyboard(getCurrentActivity(), otpValidationDialogFragment.getEditTextMobileCode());
                        if (otpValidationDialogFragment.isVisible())
                            otpValidationDialogFragment.dismiss();
                    }

                    if (mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
                        trackEvent(TrackingAware.CHECKOUT_ADDRESS_CREATED, null);
                    }
                    if (mAddress == null || forceCreate) {
                        Toast.makeText(getCurrentActivity(), R.string.addressAdded, Toast.LENGTH_LONG).show();
                        addressCreatedModified(createUpdateAddressApiResponse.apiResponseContent.address);
                    } else {
                        Toast.makeText(getCurrentActivity(), R.string.addressUpdated, Toast.LENGTH_LONG).show();
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