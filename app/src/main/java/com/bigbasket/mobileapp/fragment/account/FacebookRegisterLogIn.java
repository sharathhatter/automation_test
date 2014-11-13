package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jugal on 11/11/14.
 */
public abstract class FacebookRegisterLogIn extends BaseFragment {

    protected Session currentSession;
    protected static final List<String> PERMISSIONS = Arrays.asList("email", "user_friends", "public_profile");
    protected GraphUser user;
    protected JSONObject user_details = new JSONObject();

    protected void connectToFB() {

        /*
        openActiveSession(getActivity(), true, PERMISSIONS, callback);
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo("com.bigbasket.mobileapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign=Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("****************** MY KEY HASH:=> ", sign);
                Toast.makeText(getActivity().getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }

        */
        currentSession = new Session.Builder(getActivity()).build();
        currentSession.addCallback(callback);
        Session.OpenRequest openRequest = new Session.OpenRequest(this);
        //openRequest.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);
        openRequest.setRequestCode(Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
        openRequest.setPermissions(PERMISSIONS);
        currentSession.openForRead(openRequest);
        Session.setActiveSession(currentSession);
    }

    protected Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if(getActivity()!=null)
                onSessionStateChange(session, state, exception);
        }
    };

    protected void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        if (exception != null) {
            Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        if (session != currentSession) {
            Toast.makeText(getActivity(), "Current session expired", Toast.LENGTH_LONG).show();
            return;
        }

        if (session.isOpened()) {
            Toast.makeText(getActivity(), "Session active..", Toast.LENGTH_LONG).show();
            fetchUserInfo(session);
        } else {
            if (session.isClosed()) {
                Toast.makeText(getActivity(), "session is closed", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(Constants.FB_EMAIL_ID);
                editor.commit();
                session.closeAndClearTokenInformation();
            } else {
                Toast.makeText(getActivity(), "isFetching", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract void fetchUserInfo(Session session);

    protected void callFbLogin() {
        if (user_details != null && user_details.length() > 0) {
            HashMap<String, String> load = new HashMap<>();
            load.put(Constants.USER_DETAILS, user_details.toString());
            startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.FB_LOGIN_REGISTER, load, true, false, null);
        } else {
            showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
        }
    }

    public abstract void saveUserDetailInPreference(JsonObject responseJsonObj);

    public abstract Bundle getFBDataFromPreference(Bundle bundle, String serverErrorMsg);

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult){
        if (httpOperationResult.getUrl().contains(Constants.FB_LOGIN_REGISTER) ||
                httpOperationResult.getUrl().contains(Constants.FB_CONFIRM)) {
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
                            break;
                        case Constants.FB_CONFIRM_ERROR:
                            String errorMsg = responseJsonObj.get(Constants.MESSAGE).getAsString();
                            FBConfirmFragment fbConfirmFragment = new FBConfirmFragment();
                            Bundle bundle = getFBDataFromPreference(new Bundle(), errorMsg);
                            fbConfirmFragment.setArguments(bundle);
                            changeFragment(fbConfirmFragment);
                            break;
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
        }else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this.getActivity(), requestCode, resultCode, data);
    }
    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return FacebookRegisterLogIn.class.getName();
    }

    public LinearLayout getContentView() {
        return null;
    }
}
