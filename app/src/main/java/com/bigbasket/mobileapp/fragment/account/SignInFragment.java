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
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class SignInFragment extends FacebookRegisterLogIn {
    private static final String TAG = SignInFragment.class.getSimpleName();
    private EditText editTextEmail, editTextPasswd;
    private Button btnLogin;
    private CheckBox chkRememberMe;
    private ImageView imgEmailErr, imgPasswdErr;
    private ProgressBar progressBarLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_login, container, false);
    }


    public void fetchUserInfo(Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser me, Response response) {
                user = me;
                if (user != null) {
                    Toast.makeText(getActivity(), "start calling server for log-in", Toast.LENGTH_SHORT).show();
                    startFBLogin();
                }
                if (response.getError() != null) {
                    showErrorMsg("Error! " + response.getError());
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderLoginLayout();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SignInFragment.class.getName();
    }

    private void renderLoginLayout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View base = getView();
        assert base != null;

        chkRememberMe = (CheckBox) base.findViewById(R.id.chkRememberMe);
        chkRememberMe.setTypeface(faceRobotoRegular);
        chkRememberMe.setChecked(preferences.getBoolean(Constants.REMEMBER_ME_PREF, false));

        //progressBarLogin = (ProgressBar) base.findViewById(R.id.progressBarLogin);

        btnLogin = (Button) base.findViewById(R.id.btnLogin);
        btnLogin.setTypeface(faceRobotoRegular);
        resetLoginButton();

        Button btnFBLogin = (Button) base.findViewById(R.id.btnFBLogin);
        btnFBLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToFB();
            }
        });


        TextView txtForgotPasswd = (TextView) base.findViewById(R.id.txtForgotPasswd);
        txtForgotPasswd.setTypeface(faceRobotoRegular);
        txtForgotPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnForgotPasswordClicked();
            }
        });

        editTextEmail = (EditText) base.findViewById(R.id.editTextEmail);
        editTextPasswd = (EditText) base.findViewById(R.id.editTextPasswd);
        editTextEmail.setTypeface(faceRobotoRegular);
        editTextPasswd.setTypeface(faceRobotoRegular);
        String savedEmail = preferences.getString(Constants.EMAIL_PREF, "");
        if (!TextUtils.isEmpty(savedEmail)) {
            editTextEmail.setText(savedEmail);
        }
        String savedPwd = preferences.getString(Constants.PASSWD_PREF, "");
        if (!TextUtils.isEmpty(savedPwd)) {
            editTextPasswd.setText(savedPwd);
        }

        imgEmailErr = (ImageView) base.findViewById(R.id.imgEmailErr);
        imgPasswdErr = (ImageView) base.findViewById(R.id.imgPasswdErr);
        imgEmailErr.setVisibility(View.GONE);
        imgPasswdErr.setVisibility(View.GONE);
//        base.findViewById(R.id.sign_in_button).setOnClickListener(this);

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

    private void startFBLogin() {
        try {
            saveFBDataToPreference(user);

            user_details.put(Constants.EMAIL, user.asMap().get(Constants.EMAIL).toString());
            user_details.put(Constants.FIRSTNAME, user.getFirstName());
            user_details.put(Constants.LASTNAME, user.getLastName());
            user_details.put(Constants.FB_GENDER, user.getInnerJSONObject().getString(Constants.FB_GENDER));
            user_details.put(Constants.FB_LINK, user.getInnerJSONObject().getString(Constants.FB_LINK));
            user_details.put(Constants.FB_VERIFIED, user.getInnerJSONObject().getString(Constants.FB_VERIFIED));
            user_details.put(Constants.FB_ID, user.getId());
            String full_name = user.getFirstName();
            if (!TextUtils.isEmpty(user.getFirstName())) {
                full_name = " " + user.getLastName();
            }
            user_details.put(Constants.FB_FULL_NAME, full_name);

            // make a login call to server
            callFbLogin();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveFBDataToPreference(GraphUser user) {
        try {
            //saving fb data to preference for auto-login
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.FB_EMAIL_ID, user.asMap().get(Constants.EMAIL).toString());
            editor.putString(Constants.FIRSTNAME, user.getFirstName());
            editor.putString(Constants.LASTNAME, user.getLastName());
            editor.putString(Constants.FB_GENDER, user.getInnerJSONObject().getString(Constants.FB_GENDER));
            editor.putString(Constants.FB_LINK, user.getInnerJSONObject().getString(Constants.FB_LINK));
            editor.putString(Constants.FB_VERIFIED, user.getInnerJSONObject().getString(Constants.FB_VERIFIED));
            editor.putString(Constants.FB_ID, user.getId());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void startLogin(String email, String passwd) {
        setLoginButtonInProgress();
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.EMAIL, email);
        params.put(Constants.PASSWORD, passwd);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.LOGIN, params, true, false, null);
    }

    @Override
    public void showProgressDialog(String msg) {
        // Not showing progress bar
    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void onHttpError() {
        resetLoginButton();
    }

    private void resetLoginButton() {
        btnLogin.setText(getString(R.string.signIn));
        progressBarLogin.setVisibility(View.GONE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnLoginButtonClicked();
            }
        });
    }

    private void setLoginButtonInProgress() {
        btnLogin.setText(getString(R.string.loggingIn));
        progressBarLogin.setVisibility(View.VISIBLE);
        btnLogin.setOnClickListener(null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.LOGIN) &&
                !httpOperationResult.getUrl().contains(Constants.FB_LOGIN_REGISTER)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            resetLoginButton();
            switch (status) {
                case Constants.OK:
                    saveUserDetailInPreference(responseJsonObj);
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
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    public Bundle getFBDataFromPreference(Bundle bundle, String serverErrorMsg) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        bundle.putString(Constants.FB_SERVER_ERROR_MSG, serverErrorMsg);
        bundle.putString(Constants.FB_FIRST_NAME, preferences.getString(Constants.FIRSTNAME, null));
        bundle.putString(Constants.FB_LAST_NAME, preferences.getString(Constants.LASTNAME, null));
        bundle.putString(Constants.FB_EMAIL_ID, preferences.getString(Constants.FB_EMAIL_ID, null));
        bundle.putString(Constants.FB_GENDER, preferences.getString(Constants.FB_GENDER, null));
        bundle.putString(Constants.FB_LINK, preferences.getString(Constants.FB_LINK, null));
        bundle.putString(Constants.FB_VERIFIED, preferences.getString(Constants.FB_VERIFIED, null));
        bundle.putString(Constants.FB_ID, preferences.getString(Constants.FB_ID, null));
        return bundle;
    }

    public void saveUserDetailInPreference(JsonObject responseJsonObj) {
        if (getActivity() == null || getCurrentActivity() == null) return;
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
        if (chkRememberMe.isChecked()) {
            editor.putString(Constants.EMAIL_PREF, email);
            editor.putBoolean(Constants.REMEMBER_ME_PREF, true);
            editor.putString(Constants.PASSWD_PREF, passwd);
        } else {
            editor.remove(Constants.EMAIL_PREF);
            editor.remove(Constants.REMEMBER_ME_PREF);
            editor.remove(Constants.PASSWD_PREF);
        }
        editor.commit();
        getCurrentActivity().onLoginSuccess();
    }

    public void OnForgotPasswordClicked() {

    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutLogin) : null;
    }

    @Override
    public String getTitle() {
        return null;
    }
}