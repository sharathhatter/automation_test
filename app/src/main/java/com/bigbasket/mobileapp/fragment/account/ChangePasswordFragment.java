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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
import com.bigbasket.mobileapp.util.UIUtil;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jugal on 22/9/14.
 */
public class ChangePasswordFragment extends BaseFragment {

    private EditText oldEditText, newPwdText, confirmPwdEditText;
    private Button updateButton;
    private ProgressBar progressBarUpdatePassword;

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

        trackEvent(TrackingAware.MY_ACCOUNT_CHANGE_PASSWD_SELECTED, null);
    }


    private void setUpdateButtonInProgress() {
        updateButton.setText(getString(R.string.updating));
        progressBarUpdatePassword.setVisibility(View.VISIBLE);
        progressBarUpdatePassword.setOnClickListener(null);
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
            focusView = newPwdText;
            UIUtil.reportFormInputFieldError(newPwdText, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(confPassword)) {
            cancel = true;
            focusView = confirmPwdEditText;
            UIUtil.reportFormInputFieldError(confirmPwdEditText, getString(R.string.error_field_required));
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            cancel = true;
            focusView = newPwdText;
            UIUtil.reportFormInputFieldError(newPwdText, getString(R.string.psswordMst6Digit));
        }

        if (confPassword.length() < 6) {
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
            ((BaseActivity) getActivity()).showToast(getString(R.string.oldNewPasswordNotMatch));
            newPwdText.setText("");
            confirmPwdEditText.setText("");
            return;
        }
        setUpdateButtonInProgress();
        updatePassword();

    }

    private void updatePassword() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        resetUpdateButton();
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
                            trackEvent(TrackingAware.MY_ACCOUNT_CHANGE_PASSWD_SUCCESS, null);
                        } else {
                            onChangePasswordErrorResponse();
                            switch (changePasswordCallback.getErrorTypeAsInt()) {
                                case ApiErrorCodes.INVALID_USER_PASSED:
                                    showErrorMsg(getString(R.string.OLD_PASS_NOT_CORRECT));
                                    break;
                                default:
                                    handler.sendEmptyMessage(changePasswordCallback.getErrorTypeAsInt());
                                    break;
                            }
                            trackEvent(TrackingAware.MY_ACCOUNT_CHANGE_PASSWD_FAILED, null);
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
                        trackEvent(TrackingAware.MY_ACCOUNT_CHANGE_PASSWD_FAILED, null);
                    }
                });
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


    /*
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

    */

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
