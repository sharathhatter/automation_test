package com.bigbasket.mobileapp.fragment.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;


public class SignUpFragment extends BaseFragment {

    private EditText editTextEmail;
    private EditText editTextPasswd;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextMobileNumber;
    private Button btnRegister;
    private ProgressBar progressBarRegister;
    private CheckBox chkAcceptTerms, chkReceivePromos;
    private ImageView imgEmailErr, imgPasswdErr, imgFirstNameErr, imgLastNameErr, imgMobileNumberErr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_signup, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderRegisterLayout();
    }

    private void renderRegisterLayout() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currentCityName = prefer.getString(Constants.CITY, "");

        View base = getView();
        assert base != null;
        editTextEmail = (EditText) base.findViewById(R.id.editTextEmail);
        editTextPasswd = (EditText) base.findViewById(R.id.editTextPasswd);
        EditText editTextCity = (EditText) base.findViewById(R.id.editTextCity);
        editTextFirstName = (EditText) base.findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) base.findViewById(R.id.editTextLastName);
        editTextMobileNumber = (EditText) base.findViewById(R.id.editTextMobileNumber);

        imgEmailErr = (ImageView) base.findViewById(R.id.imgEmailErr);
        imgMobileNumberErr = (ImageView) base.findViewById(R.id.imgMobileNumberErr);
        imgPasswdErr = (ImageView) base.findViewById(R.id.imgPasswdErr);
        imgFirstNameErr = (ImageView) base.findViewById(R.id.imgFirstNameErr);
        imgLastNameErr = (ImageView) base.findViewById(R.id.imgLastNameErr);

        chkAcceptTerms = (CheckBox) base.findViewById(R.id.chkAcceptTerms);
        chkReceivePromos = (CheckBox) base.findViewById(R.id.chkReceivePromos);

        btnRegister = (Button) base.findViewById(R.id.btnRegister);
        progressBarRegister = (ProgressBar) base.findViewById(R.id.progressBarRegister);
        resetRegisterBtn();

        editTextEmail.setTypeface(faceRobotoRegular);
        editTextPasswd.setTypeface(faceRobotoRegular);
        editTextCity.setTypeface(faceRobotoRegular);
        editTextMobileNumber.setTypeface(faceRobotoRegular);
        editTextLastName.setTypeface(faceRobotoRegular);
        editTextFirstName.setTypeface(faceRobotoRegular);
        chkAcceptTerms.setTypeface(faceRobotoRegular);
        chkReceivePromos.setTypeface(faceRobotoRegular);
        btnRegister.setTypeface(faceRobotoRegular);

        editTextCity.setText(currentCityName);
    }

    private void onRegisterButtonClicked() {
        imgEmailErr.setVisibility(View.GONE);
        imgFirstNameErr.setVisibility(View.GONE);
        imgLastNameErr.setVisibility(View.GONE);
        imgMobileNumberErr.setVisibility(View.GONE);
        imgPasswdErr.setVisibility(View.GONE);

        ArrayList<String> missingFields = getMissingFields();
        if (missingFields != null && missingFields.size() > 0) {
            showErrorMsg("Please enter " + UIUtil.sentenceJoin(missingFields));
            return;
        }
        String email = editTextEmail.getText().toString();
        if (!UIUtil.isValidEmail(email)) {
            showErrorMsg("Please enter a valid email address");
            return;
        }
        if (!chkAcceptTerms.isChecked()) {
            ((BaseActivity) getActivity()).showToast(getString(R.string.acceptTermsMsg));
            return;
        }
        setRegisterBtnInProgress();
        JsonObject userDetailsJsonObj = getUserDetailsJsonObject();
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.USER_DETAILS, userDetailsJsonObj.toString());
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.REGISTER_MEMBER, params, true, false, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (getActivity() == null || getBaseActivity() == null) return;
        if (httpOperationResult.getUrl().contains(Constants.REGISTER_MEMBER)) {
            resetRegisterBtn();
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = preferences.edit();

                    String bbToken = responseJsonObj.get(Constants.BB_TOKEN).getAsString();
                    String mid = responseJsonObj.get(Constants.MID_KEY).getAsString();
                    String firstName = editTextFirstName.getText().toString();
                    String lastName = editTextLastName.getText().toString();
                    String email = editTextEmail.getText().toString();
                    editor.putString(Constants.BBTOKEN_KEY, bbToken);
                    editor.putString(Constants.MID_KEY, mid);
                    editor.putString(Constants.FIRST_NAME_PREF, firstName);
                    editor.putString(Constants.MEMBER_FULL_NAME_KEY, firstName + " " + lastName);
                    editor.putString(Constants.MEMBER_EMAIL_KEY, email);
                    editor.commit();
                    OnRegistrationSuccess(email, editTextPasswd.getText().toString());
                    break;
                case Constants.ERROR:
                    // TODO : Replace with error
                    break;
            }
        } else if (httpOperationResult.getUrl().contains(Constants.LOGIN)) {
            resetRegisterBtn();
            getBaseActivity().onLoginSuccess();
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void OnRegistrationSuccess(String email, String password) {
        setRegisterBtnInProgress();
        AuthParameters.updateInstance(getActivity());
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, password);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.LOGIN, params, true, false, null);
    }

    private JsonObject getUserDetailsJsonObject() {
        String email = editTextEmail.getText().toString();
        String passwd = editTextPasswd.getText().toString();
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String mobileNumber = editTextMobileNumber.getText().toString();

        JsonObject userDetailsJsonObj = new JsonObject();
        userDetailsJsonObj.addProperty(Constants.EMAIL, email);
        userDetailsJsonObj.addProperty(Constants.FIRSTNAME, firstName);
        userDetailsJsonObj.addProperty(Constants.LASTNAME, lastName);
        userDetailsJsonObj.addProperty(Constants.PASSWORD, passwd);
        userDetailsJsonObj.addProperty(Constants.MOBILE_NUMBER, mobileNumber);
        userDetailsJsonObj.addProperty(Constants.NEWSLETTER_SUBSCRIPTION, chkReceivePromos.isChecked());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cityId = preferences.getString(Constants.CITY_ID, "");
        userDetailsJsonObj.addProperty(Constants.CITY_ID, cityId);
        return userDetailsJsonObj;
    }

    private ArrayList<String> getMissingFields() {
        ArrayList<String> missingFields = new ArrayList<>();
        if (isFieldEmpty(editTextEmail, imgEmailErr)) {
            missingFields.add("email");
        }
        if (isFieldEmpty(editTextPasswd, imgPasswdErr)) {
            missingFields.add("password");
        }
        if (isFieldEmpty(editTextFirstName, imgFirstNameErr)) {
            missingFields.add("first-name");
        }
        if (isFieldEmpty(editTextLastName, imgLastNameErr)) {
            missingFields.add("last-name");
        }
        return missingFields;
    }

    private boolean isFieldEmpty(EditText editText, ImageView errImgVw) {
        if (TextUtils.isEmpty(editText.getText().toString())) {
            errImgVw.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void resetRegisterBtn() {
        btnRegister.setText(getString(R.string.signUp));
        progressBarRegister.setVisibility(View.GONE);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterButtonClicked();
            }
        });
    }

    private void setRegisterBtnInProgress() {
        btnRegister.setText(getString(R.string.registering));
        progressBarRegister.setVisibility(View.VISIBLE);
        btnRegister.setOnClickListener(null);
    }

    @Override
    public void showProgressDialog(String msg) {
        // Not showing progress bar
    }

    @Override
    public void hideProgressDialog() {

    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutSignup) : null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SignUpFragment.class.getName();
    }
}