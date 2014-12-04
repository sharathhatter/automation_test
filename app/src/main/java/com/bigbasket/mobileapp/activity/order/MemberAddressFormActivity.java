package com.bigbasket.mobileapp.activity.order;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ExceptionUtil;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressFormActivity extends BackButtonActivity implements PinCodeAware {

    private Address address;
    private View base;
    private String cityName;
    private EditText editTextPincode;
    private AutoCompleteTextView editTextArea;
    private Dialog numberValidateDialog;
    private TextView txtErrorValidateNumber, txtResendNumber;
    private String mErrorMsg;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getActivityTitle());
        if (address == null) {
            address = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            cityName = preferences.getString(Constants.CITY, null);
        }
        if (getSystemAreaInfo()) {
            hRefresh.sendEmptyMessage(1);
        } else {
            showForm();
        }
    }

    private void showForm() {
        FrameLayout contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = inflater.inflate(R.layout.uiv3_member_address_form, null);
        editTextArea = (AutoCompleteTextView) base.findViewById(R.id.editTextArea);
        editTextPincode = (EditText) base.findViewById(R.id.editTextPincode);
        if (base == null) {
            finish();
            return;
        }
        Button btnAddress = (Button) base.findViewById(R.id.btnAddress);
        btnAddress.setTypeface(faceRobotoRegular);
        if (address != null) {
            btnAddress.setText(getString(R.string.update));
            populateUiFields();
        } else {
            btnAddress.setText(getString(R.string.add));
            EditText editTextCity = (EditText) base.findViewById(R.id.editTextCity);
            editTextCity.setText(cityName);
        }
        setAdapterArea(editTextArea, editTextPincode);
        contentLayout.addView(base);
    }

    private void populateUiFields() {
        EditText editTextAddressNick = (EditText) base.findViewById(R.id.editTextAddressNick);
        EditText editTextFirstName = (EditText) base.findViewById(R.id.editTextFirstName);
        EditText editTextLastName = (EditText) base.findViewById(R.id.editTextLastName);
        EditText editTextContactNum = (EditText) base.findViewById(R.id.editTextContactNum);
        EditText editTextHouseNum = (EditText) base.findViewById(R.id.editTextHouseNum);
        EditText editTextStreetName = (EditText) base.findViewById(R.id.editTextStreetName);
        EditText editTextResidentialComplex = (EditText) base.findViewById(R.id.editTextResidentialComplex);
        EditText editTextLandmark = (EditText) base.findViewById(R.id.editTextLandmark);
        EditText editTextCity = (EditText) base.findViewById(R.id.editTextCity);
        CheckBox chkIsAddrDefault = (CheckBox) base.findViewById(R.id.chkIsAddrDefault);

        editTextAddressNick.setText(getValueOrBlank(address.getAddressNickName()));
        editTextFirstName.setText(getValueOrBlank(address.getFirstName()));
        editTextLastName.setText(getValueOrBlank(address.getLastName()));
        editTextContactNum.setText(getValueOrBlank(address.getContactNum()));
        editTextHouseNum.setText(getValueOrBlank(address.getHouseNumber()));
        editTextStreetName.setText(getValueOrBlank(address.getStreet()));
        editTextResidentialComplex.setText(getValueOrBlank(address.getResidentialComplex()));
        editTextLandmark.setText(getValueOrBlank(address.getLandmark()));
        editTextArea.setText(getValueOrBlank(address.getArea()));
        editTextCity.setText(getValueOrBlank(address.getCityName()));
        editTextPincode.setText(getValueOrBlank(address.getPincode()));
        chkIsAddrDefault.setChecked(address.isDefault()); // Don't remove since during request, this is read
        chkIsAddrDefault.setVisibility(address.isDefault() ? View.GONE : View.VISIBLE);
    }

    private void uploadAddress(String otp_code) {
        final EditText editTextAddressNick = (EditText) base.findViewById(R.id.editTextAddressNick);
        final EditText editTextFirstName = (EditText) base.findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = (EditText) base.findViewById(R.id.editTextLastName);
        final EditText editTextContactNum = (EditText) base.findViewById(R.id.editTextContactNum);
        final EditText editTextHouseNum = (EditText) base.findViewById(R.id.editTextHouseNum);
        final EditText editTextStreetName = (EditText) base.findViewById(R.id.editTextStreetName);
        final EditText editTextResidentialComplex = (EditText)
                base.findViewById(R.id.editTextResidentialComplex);
        final EditText editTextLandmark = (EditText) base.findViewById(R.id.editTextLandmark);
        final CheckBox chkIsAddrDefault = (CheckBox) base.findViewById(R.id.chkIsAddrDefault);

        editTextAddressNick.setError(null);
        editTextFirstName.setError(null);
        editTextLastName.setError(null);
        editTextContactNum.setError(null);
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
        if (isEditTextEmpty(editTextLastName)) {
            reportFormInputFieldError(editTextLastName, getString(R.string.error_field_required));
            focusView = editTextLastName;
            cancel = true;
        }
        if (isEditTextEmpty(editTextContactNum)) {
            reportFormInputFieldError(editTextContactNum, getString(R.string.error_field_required));
            focusView = editTextContactNum;
            cancel = true;
        } else if (editTextContactNum.getText().toString().length() < 10) {
            reportFormInputFieldError(editTextContactNum, getString(R.string.contactNoMin10));
            focusView = editTextContactNum;
            cancel = true;
        }
        if (isEditTextEmpty(editTextHouseNum)) {
            reportFormInputFieldError(editTextHouseNum, getString(R.string.error_field_required));
            focusView = editTextHouseNum;
            cancel = true;
        }
        if (isEditTextEmpty(editTextArea)) {
            reportFormInputFieldError(editTextArea, getString(R.string.error_field_required));
            focusView = editTextArea;
            cancel = true;
        }
        if (isEditTextEmpty(editTextPincode)) {
            reportFormInputFieldError(editTextPincode, getString(R.string.error_field_required));
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
                put(Constants.CONTACT_NUM, editTextContactNum.getText().toString());
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

        if (otp_code != null) {
            payload.put(Constants.OTP_CODE, otp_code);
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
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

    class CreateUpdateAddressApiCallback implements Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> {

        @Override
        public void success(ApiResponse<CreateUpdateAddressApiResponseContent> createUpdateAddressApiResponse, Response response) {
            hideProgressDialog();
            switch (createUpdateAddressApiResponse.status) {
                case 0:
                    if (address == null) {
                        Toast.makeText(getCurrentActivity(), "Address updated successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified(createUpdateAddressApiResponse.apiResponseContent.addressId);
                    } else {
                        Toast.makeText(getCurrentActivity(), "Address updated successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified();
                    }
                    if (numberValidateDialog != null && numberValidateDialog.isShowing())
                        numberValidateDialog.dismiss();
                    break;
                case Constants.NUMBER_USED_BY_ANOTHER_MEMBER:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    hRefresh.sendEmptyMessage(Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER);
                    break;
                case Constants.OPT_NEEDED:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    hRefresh.sendEmptyMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP);
                    break;
                case Constants.INVALID_OTP:
                    mErrorMsg = createUpdateAddressApiResponse.message;
                    hRefresh.sendEmptyMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG);
                    break;
                case ExceptionUtil.INTERNAL_SERVER_ERROR:
                    showAlertDialog(getCurrentActivity(), "BigBasket", "Server Error");
                    break;
                default:
                    String msg = createUpdateAddressApiResponse.message;
                    showAlertDialog(getCurrentActivity(), "BigBasket", msg);
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            hideProgressDialog();
            showAlertDialog(getCurrentActivity(), null, "Server Error");
            // TODO : Improve error handling
        }
    }

    private void addressCreatedModified(String addressId) {
        Intent result = new Intent();
        result.putExtra(Constants.MEMBER_ADDRESS_ID, addressId);
        setResult(Constants.ADDRESS_CREATED_MODIFIED, result);
        finish();
    }

    private void addressCreatedModified() {
        setResult(Constants.ADDRESS_CREATED_MODIFIED);
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
            address = getIntent().getParcelableExtra(Constants.MEMBER_ADDRESS_ID);
        }
        return address == null ? "Create new address" : "Update address";
    }

    public void OnSubmitButtonClicked(View v) {
        uploadAddress(null);
    }

    private void validateMobileNumber(boolean txtErrorValidateNumberVisibility, String errorMsg) {
        if (numberValidateDialog != null && numberValidateDialog.isShowing()) {
            if (txtErrorValidateNumberVisibility) {
                txtErrorValidateNumber.setText(errorMsg != null ? errorMsg : getResources().getString(R.string.otpCodeErrorMsg));
                txtErrorValidateNumber.setVisibility(View.VISIBLE);
                txtResendNumber.setVisibility(View.GONE);
            }
            return;
        }
        numberValidateDialog = new Dialog(getCurrentActivity());
        numberValidateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        numberValidateDialog.setContentView(R.layout.confirm_mobile_number);
        TextView txtDialogTitle = (TextView) numberValidateDialog.findViewById(R.id.txtDialogTitle);
        TextView txtHeaderMsg = (TextView) numberValidateDialog.findViewById(R.id.txtHeaderMsg);
        final TextView editTextMobileCode = (TextView) numberValidateDialog.findViewById(R.id.editTextMobileCode);
        txtHeaderMsg.setTypeface(faceRobotoRegular);
        txtResendNumber = (TextView) numberValidateDialog.findViewById(R.id.txtResendNumber);
        txtErrorValidateNumber = (TextView) numberValidateDialog.findViewById(R.id.txtErrorValidateNumber);
        TextView txtResendCode = (TextView) numberValidateDialog.findViewById(R.id.txtResendCode);
        txtResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtErrorValidateNumber.setVisibility(View.GONE);
                txtResendNumber.setVisibility(View.VISIBLE);
                uploadAddress(null);
            }
        });
        txtDialogTitle.setText(getResources().getString(R.string.confirmMobileNumber));
        TextView txtConfirm = (TextView) numberValidateDialog.findViewById(R.id.txtConfirm);
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editTextMobileCode.getText().toString())) {
                    txtErrorValidateNumber.setText(getResources().getString(R.string.pleaseEnterTxt));
                    txtErrorValidateNumber.setVisibility(View.VISIBLE);
                } else if (editTextMobileCode.getText() != null && editTextMobileCode.getText().toString().length() > 0) {
                    uploadAddress(editTextMobileCode.getText().toString());
                } else {
                    txtErrorValidateNumber.setText(getResources().getString(R.string.otpCodeErrorMsg));
                    txtErrorValidateNumber.setVisibility(View.VISIBLE);
                }

            }
        });

        TextView txtCancel = (TextView) numberValidateDialog.findViewById(R.id.txtCancel);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberValidateDialog.isShowing()) {
                    numberValidateDialog.dismiss();
                } else {
                    return;
                }
            }
        });


        ImageView imgCloseBtn = (ImageView) numberValidateDialog.findViewById(R.id.imgCloseBtn);
        imgCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberValidateDialog.isShowing()) {
                    numberValidateDialog.dismiss();
                } else {
                    return;
                }
            }
        });
        numberValidateDialog.show();
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null && sourceName.equalsIgnoreCase(Constants.ERROR)) {
            finish();
        }
        super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
    }

    @Override
    public void showProgressView() {
        showProgressDialog(getString(R.string.please_wait));
    }

    @Override
    public void hideProgressView() {
        hideProgressDialog();
    }

    Handler hRefresh = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
                    showProgressView();
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
    };
}