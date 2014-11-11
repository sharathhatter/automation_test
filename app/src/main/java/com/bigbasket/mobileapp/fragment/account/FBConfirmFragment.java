package com.bigbasket.mobileapp.fragment.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
//import com.melnykov.fab.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class FBConfirmFragment extends BaseFragment {

    private String serverErrorMsg;
    private EditText editTextEmail, editTextPasswd;
    private String firstName, lastName, emailID;
    private ImageView imgEmailErr, imgPasswdErr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        serverErrorMsg = bundle.getString(Constants.FB_SERVER_ERROR_MSG);
        firstName = bundle.getString(Constants.FB_FIRST_NAME);
        lastName = bundle.getString(Constants.FB_LAST_NAME);
        emailID = bundle.getString(Constants.FB_EMAIL_ID);
        renderFbConfirmForm();
    }

    private void renderFbConfirmForm() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_fb_confirm_layout, null);
        editTextEmail = (EditText) base.findViewById(R.id.editTextEmail);
        editTextPasswd = (EditText) base.findViewById(R.id.editTextPasswd);
        editTextEmail.setTypeface(faceRobotoRegular);
        editTextPasswd.setTypeface(faceRobotoRegular);
        TextView fbConfirmMsg1 = (TextView) base.findViewById(R.id.fbConfirmMsg1);
        fbConfirmMsg1.append(" " + firstName + " " + lastName + " ( " + emailID + " ).");
        TextView fbConfirmMsg2 = (TextView) base.findViewById(R.id.fbConfirmMsg2);
        fbConfirmMsg2.append(" " + emailID + ".");

        TextView fbConfirmMsg3 = (TextView) base.findViewById(R.id.fbConfirmMsg3);
        fbConfirmMsg3.setTypeface(faceRobotoRegular);

        imgEmailErr = (ImageView) base.findViewById(R.id.imgEmailErr);
        imgPasswdErr = (ImageView) base.findViewById(R.id.imgPasswdErr);

        final LinearLayout layoutEmailConfirmForm = (LinearLayout) base.findViewById(R.id.layoutEmailConfirmForm);
        final LinearLayout layoutDoNotHaveAcc = (LinearLayout) base.findViewById(R.id.layoutDoNotHaveAcc);
        final RadioButton radioBtnYesHaveAcc = (RadioButton) base.findViewById(R.id.radioBtnYesHaveAcc);
        TextView txtYesHaveAcc = (TextView) base.findViewById(R.id.txtYesHaveAcc);
        txtYesHaveAcc.setText("Link " + firstName + " " + lastName + " " + getString(R.string.txtYesHaveAccMsg));
        final RadioButton radioBtnNoHaveAcc = (RadioButton) base.findViewById(R.id.radioBtnNoHaveAcc);

//        final FloatingActionButton btnFBConfirm = (FloatingActionButton) base.findViewById(R.id.btnFBConfirm);
//        btnFBConfirm.setTag(getString(R.string.txtLinkAccount));
//        btnFBConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (String.valueOf(btnFBConfirm.getTag()).equalsIgnoreCase(getString(R.string.txtLinkAccount))) {
//                    OnLoginButtonClicked();
//                } else {
//                    callFbConfirmToLinkMailIDTOBigbasketId();
//                }
//            }
//        });
        final TextView txtFBConfirmBtnHint = (TextView) base.findViewById(R.id.txtFBConfirmBtnHint);

        radioBtnYesHaveAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (radioBtnYesHaveAcc.isChecked()) {
                    radioBtnNoHaveAcc.setChecked(false);
                    layoutDoNotHaveAcc.setVisibility(View.GONE);
                    layoutEmailConfirmForm.setVisibility(View.VISIBLE);
//                    btnFBConfirm.setTag(getString(R.string.txtLinkAccount));
//                    txtFBConfirmBtnHint.setText(String.valueOf(btnFBConfirm.getTag()));
                }
            }
        });

        radioBtnNoHaveAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (radioBtnNoHaveAcc.isChecked()) {
                    radioBtnYesHaveAcc.setChecked(false);
                    layoutEmailConfirmForm.setVisibility(View.GONE);
                    layoutDoNotHaveAcc.setVisibility(View.VISIBLE);
//                    btnFBConfirm.setTag(getString(R.string.txtCreateAndLinkAccount));
//                    txtFBConfirmBtnHint.setText(String.valueOf(btnFBConfirm.getTag()));
                }
            }
        });
        TextView txtCreateNewAccount = (TextView) base.findViewById(R.id.txtCreateNewAccount);
        txtCreateNewAccount.setText(serverErrorMsg);

        /*
        TextView txtLinkAcc = (TextView)base.findViewById(R.id.txtLinkAcc);
        txtLinkAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnLoginButtonClicked();
            }
        });



        TextView txtLinkAccAndCreate = (TextView) base.findViewById(R.id.txtLinkAccAndCreate);
        txtLinkAccAndCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callFbConfirmToLinkMailIDTOBigbasketId();
            }
        });

        */

        contentView.addView(base);
    }


    private void callFbConfirmToLinkMailIDTOBigbasketId() {
        try {
            JSONObject user_details = new JSONObject();
            user_details.put(Constants.EMAIL, emailID);
            user_details.put(Constants.FIRSTNAME, firstName);
            user_details.put(Constants.LASTNAME, lastName);
            if (user_details.length() > 0) {
                HashMap<String, String> load = new HashMap<>();
                load.put(Constants.FB_LINK_FROM, "Login");
                load.put(Constants.FB_CONFIRM_TYPE, "create-account");
                load.put(Constants.USER_DETAILS, user_details.toString());
                startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.FB_CONFIRM, load, true, false, null);
            } else {
                showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void OnLoginButtonClicked() {
        imgEmailErr.setVisibility(View.GONE);
        imgPasswdErr.setVisibility(View.GONE);

        String email = editTextEmail.getText().toString();
        String passwd = editTextPasswd.getText().toString();

        ArrayList<String> missingFields = new ArrayList<>();
        if (TextUtils.isEmpty(email)) {
            imgEmailErr.setVisibility(View.VISIBLE);
            missingFields.add("email");
        }
        if (TextUtils.isEmpty(passwd)) {
            imgPasswdErr.setVisibility(View.VISIBLE);
            missingFields.add("password");
        }
        if (missingFields.size() > 0) {
            showErrorMsg("Please enter " + UIUtil.sentenceJoin(missingFields));
            return;
        }

        if (!UIUtil.isValidEmail(email)) {
            imgEmailErr.setVisibility(View.VISIBLE);
            showErrorMsg("Please enter a valid email");
            return;
        }

        startLogin(email, passwd);
    }

    private void startLogin(String email, String passwd) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, passwd);
        params.put("fb-login", "fb-login");
        params.put(Constants.FB_GENDER, passwd);
        params.put(Constants.FB_LINK, passwd);
        params.put(Constants.FB_VERIFIED, passwd);
        params.put(Constants.FB_ID, passwd);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.LOGIN, params, true, false, null);
    }

    private void removeFbDataFromPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.FIRSTNAME);
        editor.remove(Constants.LASTNAME);
        editor.remove(Constants.FB_EMAIL_ID);
        editor.remove(Constants.FB_GENDER);
        editor.remove(Constants.FB_LINK);
        editor.remove(Constants.FB_VERIFIED);
        editor.remove(Constants.FB_ID);
        editor.commit();

    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.LOGIN)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    removeFbDataFromPreference();
                    saveUserDetailInPreference(responseJsonObj);
                    OnLoginSuccess();
                    break;
                case Constants.ERROR:
                    //TODO : Replace with handler
                    String errorType = responseJsonObj.get(Constants.ERROR_TYPE).getAsString();
                    switch (errorType) {
                        case Constants.INVALID_USER_PASS:
                            showErrorMsg(getString(R.string.INVALID_USER_PASS));
                            break;
                        default:
                            showErrorMsg(getString(R.string.server_error));
                            break;
                    }
            }
        } else if (httpOperationResult.getUrl().contains(Constants.FB_CONFIRM)) {
            int responseCode = httpOperationResult.getResponseCode();
            String responseJsonString = httpOperationResult.getReponseString();
            if (responseCode == Constants.successRespCode) {
                if (responseJsonString != null) {
                    JsonObject responseJsonObj = new JsonParser().parse(responseJsonString).getAsJsonObject();
                    int status = responseJsonObj.get(Constants.STATUS).getAsInt();
                    switch (status) {
                        case 0:
                            JsonObject jsonObjectResponse = responseJsonObj.get(Constants.RESPONSE).getAsJsonObject();
                            saveUserDetailInPreference(jsonObjectResponse);
                            OnLoginSuccess();
                        case Constants.FB_INTERNAL_SERVER_ERROR:
                            showAlertDialogFinish(getActivity(), null, getString(R.string.INTERNAL_SERVER_ERROR));
                            break;
                        default:
                            String errorMsgDefault = responseJsonObj.get(Constants.MESSAGE).getAsString();
                            showErrorMsg(errorMsgDefault);
                            break;
                    }
                } else {
                    showAlertDialogFinish(getActivity(), null, getString(R.string.INTERNAL_SERVER_ERROR));
                }
            } else {
                showAlertDialogFinish(getActivity(), null, getString(R.string.INTERNAL_SERVER_ERROR));
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    public void OnLoginSuccess() {
        AuthParameters.updateInstance(getActivity());
        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.goToHome();
    }

    private void saveUserDetailInPreference(JsonObject responseJsonObj) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        String bbToken = responseJsonObj.get(Constants.BB_TOKEN).getAsString();
        String mid = responseJsonObj.get(Constants.MID_KEY).getAsString();
        JsonObject userDetailsJsonObj = responseJsonObj.get("user_details").getAsJsonObject();
        String firstName = userDetailsJsonObj.get("first_name").getAsString();
        String lastName = userDetailsJsonObj.get("last_name").getAsString();
        String fullName = firstName + " " + lastName;
        String email = editTextEmail.getText().toString();
        String passwd = editTextPasswd.getText().toString();
        editor.putString(Constants.FIRST_NAME_PREF, firstName);
        editor.putString(Constants.BBTOKEN_KEY, bbToken);
        editor.putString(Constants.MID_KEY, mid);
        editor.putString(Constants.MEMBER_FULL_NAME_KEY, fullName);
        editor.putString(Constants.MEMBER_EMAIL_KEY, email);
        /*
        if (chkRememberMe.isChecked()) {
            editor.putString(Constants.EMAIL_PREF, email);
            editor.putBoolean(Constants.REMEMBER_ME_PREF, true);
            editor.putString(Constants.PASSWD_PREF, passwd);
        } else {
            editor.remove(Constants.EMAIL_PREF);
            editor.remove(Constants.REMEMBER_ME_PREF);
            editor.remove(Constants.PASSWD_PREF);
        }
        */
        editor.commit();
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }


    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return FBConfirmFragment.class.getName();
    }
}
