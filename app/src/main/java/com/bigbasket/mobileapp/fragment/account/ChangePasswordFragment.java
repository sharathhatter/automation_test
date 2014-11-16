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
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jugal on 22/9/14.
 */
public class ChangePasswordFragment extends BaseFragment {

    private EditText oldEditText, newPwdText, confirmPwdEditText;
    private Button updateButton;
    private ProgressBar progressBarUpdatePassword;
    private ImageView imgOldPwdErr, imgNewPwdErr, imgConfPwdErr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_change_password, container, false);
        initiateChangePassword(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void initiateChangePassword(View base) {

        oldEditText = (EditText) base.findViewById(R.id.oldPwdEditTxt);
        oldEditText.setTypeface(faceRobotoRegular);

        newPwdText = (EditText) base.findViewById(R.id.newPassWordEditTxt);
        newPwdText.setTypeface(faceRobotoRegular);

        progressBarUpdatePassword = (ProgressBar) base.findViewById(R.id.progressBarUpdatePassword);

        imgOldPwdErr = (ImageView) base.findViewById(R.id.imgOldPasswdErr);
        imgNewPwdErr = (ImageView) base.findViewById(R.id.imgNewPasswdErr);
        imgConfPwdErr = (ImageView) base.findViewById(R.id.imgConfPasswdErr);

        confirmPwdEditText = (EditText) base.findViewById(R.id.confirmPwdEditTxt);
        confirmPwdEditText.setTypeface(faceRobotoRegular);

        updateButton = (Button) base.findViewById(R.id.btnUpdate);
        updateButton.setTypeface(faceRobotoRegular);

        BaseActivity.showKeyboard(oldEditText);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnUpdateBtnClick();
            }
        });

    }


    private void setUpdateButtonInProgress() {
        updateButton.setText(getString(R.string.updating));
        progressBarUpdatePassword.setVisibility(View.VISIBLE);
        progressBarUpdatePassword.setOnClickListener(null);
    }

    private void OnUpdateBtnClick() {
        imgOldPwdErr.setVisibility(View.GONE);
        imgNewPwdErr.setVisibility(View.GONE);
        imgConfPwdErr.setVisibility(View.GONE);
        String oldPassword = oldEditText.getText().toString();
        String newPassword = newPwdText.getText().toString();
        String confPassword = confirmPwdEditText.getText().toString();
        List<String> missingFields = new ArrayList<>();
        if (TextUtils.isEmpty(oldPassword)) {
            oldEditText.setText("");
            imgOldPwdErr.setVisibility(View.VISIBLE);
            missingFields.add("old password");
        }
        if (TextUtils.isEmpty(newPassword)) {
            newPwdText.setText("");
            imgNewPwdErr.setVisibility(View.VISIBLE);
            missingFields.add("current password");
        }
        if (TextUtils.isEmpty(confPassword)) {
            confirmPwdEditText.setText("");
            imgConfPwdErr.setVisibility(View.VISIBLE);
            missingFields.add("confirm password");
        }
        if (missingFields.size() > 0) {
            showErrorMsg("Please enter " + UIUtil.sentenceJoin(missingFields));
        } else if (newPassword.length() < 6 || confPassword.length() < 6 || oldPassword.length() < 6) {
            if (newPwdText.getText().toString().length() < 6)
                newPwdText.setText("");
            if (confirmPwdEditText.getText().toString().length() < 6)
                confirmPwdEditText.setText("");
            if (oldEditText.getText().toString().length() < 6)
                oldEditText.setText("");
            else {
                newPwdText.setText("");
                confirmPwdEditText.setText("");
                oldEditText.setText("");
            }
            ((BaseActivity) getActivity()).showToast(getString(R.string.psswordMst6Digit));

        } else if (!newPassword.equals(confPassword)) {
            newPwdText.setText("");
            confirmPwdEditText.setText("");
            ((BaseActivity) getActivity()).showToast(getString(R.string.oldNewPasswordNotMatch));
        } else {
            setUpdateButtonInProgress();
            updatePassword();

        }
    }

    private void updatePassword() {
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.CHANGE_PASSWORD,
                new HashMap<String, String>() {
                    {
                        put(Constants.OLD_PASSWORD, oldEditText.getText().toString());
                    }

                    {
                        put(Constants.NEW_PASSWORD, newPwdText.getText().toString());
                    }

                    {
                        put(Constants.CONFIRM_PASSWORD, confirmPwdEditText.getText().toString());
                    }
                }, true, false, null);
    }

    private void resetUpdateButton() {
        updateButton.setText(getString(R.string.UPDATE));
        progressBarUpdatePassword.setVisibility(View.GONE);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnUpdateBtnClick();
            }
        });
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.CHANGE_PASSWORD)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            resetUpdateButton();
            switch (status) {
                case Constants.OK:
                    onChangePasswordSuccessResponse();
                    break;
                case Constants.ERROR:
                    //TODO : Replace with handler
                    String errorType = responseJsonObj.get(Constants.ERROR_TYPE).getAsString();
                    onChangePasswordErrorResponse();
                    switch (errorType) {
                        case Constants.INVALID_USER_PASS:
                            showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                            break;
                        default:
                            showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
                            break;
                    }
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void onChangePasswordSuccessResponse() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.contains(Constants.PASSWD_PREF)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PASSWD_PREF, newPwdText.getText().toString());
            editor.commit();
        }
        ((BaseActivity) getActivity()).showToast(getString(R.string.passwordUpdated));
        finishFragment();

    }

    private void onChangePasswordErrorResponse() {
        oldEditText.setText("");
        newPwdText.setText("");
        confirmPwdEditText.setText("");
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.changePasswordLayout) : null;
    }

    @Override
    public String getTitle() {
        return "Change Password";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ChangePasswordFragment.class.getName();
    }
}
