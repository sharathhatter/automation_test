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
import android.util.Log;
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
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MemberAddressFormActivity extends BackButtonActivity {

    private Address address;
    private View base;
    private String cityName;
    private EditText editTextPincode;
    private AutoCompleteTextView editTextArea;
    private Dialog numberValidateDialog;
    private TextView txtErrorValidateNumber, txtResendNumber;
    private String errorMsg;

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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showForm();
        setAdapterArea(editTextArea, editTextPincode);
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

        // Validation
        ArrayList<String> missingFields = new ArrayList<>();
        int errorSymbolResx = R.drawable.error_symbol;
        if (isEditTextEmpty(editTextFirstName)) {
            missingFields.add(editTextFirstName.getHint().toString());
            editTextFirstName.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextFirstName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (isEditTextEmpty(editTextLastName)) {
            missingFields.add(editTextLastName.getHint().toString());
            editTextLastName.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextLastName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (isEditTextEmpty(editTextContactNum)) {
            missingFields.add(editTextContactNum.getHint().toString());
            editTextContactNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextContactNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (isEditTextEmpty(editTextHouseNum)) {
            missingFields.add(editTextHouseNum.getHint().toString());
            editTextHouseNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextHouseNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (isEditTextEmpty(editTextArea)) {
            missingFields.add(editTextArea.getHint().toString());
            editTextArea.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextArea.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (isEditTextEmpty(editTextPincode)) {
            missingFields.add(editTextPincode.getHint().toString());
            editTextPincode.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
        } else {
            editTextPincode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        int missingFieldsListSize = missingFields.size();
        if (missingFieldsListSize > 0) {
            String msg;
            if (missingFieldsListSize == 1) {
                msg = missingFields.get(0) + " is mandatory";
            } else {
                msg = "Following fields are mandatory: " + UIUtil.sentenceJoin(missingFields);
            }
            showAlertDialog(this, null, msg);
            return;
        }

        if (editTextContactNum.getText().toString().length() < 10) {
            showAlertDialog(this, null, getResources().getString(R.string.contactNoMin10));
            editTextContactNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, errorSymbolResx, 0);
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
        String url = Constants.CREATE_ADDRESS;
        if (address != null) {
            url = Constants.UPDATE_ADDRESS;
            payload.put(Constants.ID, address.getId());
        }

        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + url, payload, true,
                AuthParameters.getInstance(this), new BasicCookieStore());
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        if (httpOperationResult.getUrl().contains(Constants.GET_AREA_INFO)) {
            String responseJson = httpOperationResult.getReponseString();
            int responseCode = httpOperationResult.getResponseCode();
            int j = 0;
            AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(this);
            areaPinInfoAdapter.deleteTable();
            if (responseCode == Constants.successRespCode) {
                Log.d("Response Code", "" + responseCode);
                try {
                    if (responseJson != null) {
                        JSONObject responseJSONObject = new JSONObject(responseJson);

                        JSONObject responseJSON = responseJSONObject.getJSONObject("response");
                        JSONObject pinCodeObj = responseJSON.getJSONObject("pincode_map");

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                switch (responseCode) {
                    default:
                        String defaultMsg = "Please try again later";
                        showAlertDialogFinish(this, null, defaultMsg);
                        break;
                }

            }
        } else {
            JsonObject jsonObject = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    if (address == null) {
                        JsonObject responseJsonObj = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                        String addressId = responseJsonObj.get(Constants.ID).getAsString();
                        Toast.makeText(getCurrentActivity(), "Address updated successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified(addressId);
                    } else {
                        Toast.makeText(getCurrentActivity(), "Address updated successfully", Toast.LENGTH_LONG).show();
                        addressCreatedModified();
                    }
                    if (numberValidateDialog != null && numberValidateDialog.isShowing())
                        numberValidateDialog.dismiss();
                    break;
                case Constants.NUMBER_USED_BY_ANOTHER_MEMBER:
                    errorMsg = jsonObject.get(Constants.MESSAGE).getAsString();
                    hRefresh.sendEmptyMessage(Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER);
                    break;
                case Constants.OPT_NEEDED:
                    errorMsg = jsonObject.get(Constants.MESSAGE).getAsString();
                    hRefresh.sendEmptyMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP);
                    break;
                case Constants.INVALID_OTP:
                    errorMsg = jsonObject.get(Constants.MESSAGE).getAsString();
                    hRefresh.sendEmptyMessage(Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG);
                    break;
                case ExceptionUtil.INTERNAL_SERVER_ERROR:
                    showAlertDialog(this, "BigBasket", "Server Error");
                    break;
                default:
                    String msg = jsonObject.get(Constants.MESSAGE).getAsString();
                    showAlertDialog(this, "BigBasket", msg);
                    break;
            }
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
    public void onHttpError() {
        showAlertDialog(this, null, "Server Error");
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null && sourceName.equalsIgnoreCase(Constants.ERROR)) {
            finish();
        }
        super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
    }

    Handler hRefresh = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
                    startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_AREA_INFO, null, false,
                            authParameters, new BasicCookieStore());
                    break;
                case Constants.VALIDATE_MOBILE_NUMBER_POPUP:
                    validateMobileNumber(false, errorMsg);
                    break;
                case Constants.VALIDATE_MOBILE_NUMBER_POPUP_ERROR_MSG:
                    validateMobileNumber(true, errorMsg);
                    break;
                case Constants.MOBILE_NUMBER_USED_BY_ANOTHER_MEMBER:
                    showAlertDialog(errorMsg != null ? errorMsg : getResources().getString(R.string.numberUsedByAnotherMember));
                    break;
            }
        }
    };
}