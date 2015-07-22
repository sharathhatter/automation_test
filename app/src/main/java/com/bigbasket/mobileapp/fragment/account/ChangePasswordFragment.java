package com.bigbasket.mobileapp.fragment.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ChangePasswordFragment extends BaseFragment {

    private EditText oldEditText, newPwdText, confirmPwdEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_change_password, container, false);
        initiateChangePassword(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCurrentNavigationContext(TrackEventkeys.ACCOUNT_MENU);
    }
    public void initiateChangePassword(View base) {

        oldEditText = (EditText) base.findViewById(R.id.oldPwdEditTxt);
        oldEditText.setTypeface(faceRobotoRegular);

        newPwdText = (EditText) base.findViewById(R.id.newPassWordEditTxt);
        newPwdText.setTypeface(faceRobotoRegular);

        confirmPwdEditText = (EditText) base.findViewById(R.id.confirmPwdEditTxt);
        confirmPwdEditText.setTypeface(faceRobotoRegular);

        TextView txtUpdatePassword = (TextView) base.findViewById(R.id.txtUpdatePassword);
        txtUpdatePassword.setTypeface(faceRobotoRegular);

        BaseActivity.showKeyboard(oldEditText);

        txtUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnUpdateBtnClick();
            }
        });

        trackEvent(TrackingAware.CHANGE_PASSWORD_SHOWN, null);
    }


    private void OnUpdateBtnClick() {
        oldEditText.setError(null);
        newPwdText.setError(null);
        confirmPwdEditText.setError(null);
        String oldPassword = oldEditText.getText().toString();
        String newPassword = newPwdText.getText().toString();
        String confPassword = confirmPwdEditText.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(oldPassword)) {
            cancel = true;
            focusView = oldEditText;
            UIUtil.reportFormInputFieldError(oldEditText, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(newPassword)) {
            cancel = true;
            if (focusView == null) focusView = newPwdText;
            UIUtil.reportFormInputFieldError(newPwdText, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(confPassword)) {
            cancel = true;
            if (focusView == null) focusView = confirmPwdEditText;
            UIUtil.reportFormInputFieldError(confirmPwdEditText, getString(R.string.error_field_required));
        }

        if (newPassword.length() < 6 && !cancel) {
            cancel = true;
            focusView = newPwdText;
            UIUtil.reportFormInputFieldError(newPwdText, getString(R.string.psswordMst6Digit));
        }

        if (confPassword.length() < 6 && !cancel) {
            cancel = true;
            focusView = confirmPwdEditText;
            UIUtil.reportFormInputFieldError(confirmPwdEditText, getString(R.string.psswordMst6Digit));
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        if (!newPassword.equals(confPassword)) {
            newPwdText.requestFocus();
            showErrorMsg(getString(R.string.oldNewPasswordNotMatch));
            newPwdText.setText("");
            confirmPwdEditText.setText("");
            return;
        }
        updatePassword();
    }

    private void updatePassword() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.changePassword(oldEditText.getText().toString(), newPwdText.getText().toString(),
                confirmPwdEditText.getText().toString(),
                new Callback<OldBaseApiResponse>() {
                    @Override
                    public void success(OldBaseApiResponse changePasswordCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (changePasswordCallback.status.equals(Constants.OK)) {
                            onChangePasswordSuccessResponse();
                        } else {
                            onChangePasswordErrorResponse(changePasswordCallback.message);
                            switch (changePasswordCallback.getErrorTypeAsInt()) {
                                case ApiErrorCodes.INVALID_USER_PASSED:
                                    showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                                    break;
                                default:
                                    handler.sendEmptyMessage(changePasswordCallback.getErrorTypeAsInt(),
                                            changePasswordCallback.message);
                                    break;
                            }
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
                        logChangePasswordErrorEvent(error.toString(), TrackingAware.CHANGE_PASSWORD_FAILED);
                    }
                });
    }

    private void onChangePasswordSuccessResponse() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.contains(Constants.PASSWD_PREF)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PASSWD_PREF, newPwdText.getText().toString());
            editor.commit();
        }
        ((BaseActivity) getActivity()).showToast(getString(R.string.passwordUpdated));
        finish();

    }

    private void logChangePasswordErrorEvent(String errorMsg, String eventName) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.FAILURE_REASON, errorMsg);
        trackEvent(eventName, eventAttribs);
    }

    private void onChangePasswordErrorResponse(String errorMsg) {
        logChangePasswordErrorEvent(errorMsg, TrackingAware.CHANGE_PASSWORD_FAILED);
        oldEditText.setText("");
        newPwdText.setText("");
        confirmPwdEditText.setText("");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getCurrentActivity() != null && oldEditText != null) {
            BaseActivity.hideKeyboard(getCurrentActivity(), oldEditText);
        }
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.changePasswordLayout) : null;
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

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_CHANGE_PASSWORD_SCREEN;
    }
}
