package com.bigbasket.mobileapp.activity.account.uiv3;

import android.Manifest;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.handler.click.OnCompoundDrawableClickListener;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.OnOtpReceivedListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.OtpResponse;
import com.bigbasket.mobileapp.receivers.OTPBroadcastReceiver;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;


public class ForgotPasswordActivity extends BackButtonActivity implements OnOtpReceivedListener {

    OTPBroadcastReceiver otpBroadcastReceiver;
    private EditText otpEditTxt, newPwdEditText, confirmPwdEditText;
    private TextView txtUpdatePassword;
    private final TextWatcher OTPeditextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length() >= 4) {
                handleEditTextnUpdateViewState(true);
            } else {
                handleEditTextnUpdateViewState(false);
            }
        }
    };
    private boolean mIsPasswordVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.reset_password));

        setCurrentScreenName(TrackEventkeys.NC_FORGOT_PASSWORD_OTP);

        String emailId = getIntent().getStringExtra(Constants.EMAIL);
        if (TextUtils.isEmpty(emailId)) {
            Crashlytics.log(getString(R.string.email_not_found));
            finish();
            return;
        }
        if (savedInstanceState == null) {
            if (handlePermission(Manifest.permission.RECEIVE_SMS,
                    getString(R.string.sms_permission_rationale),
                    Constants.PERMISSION_REQUEST_CODE_RECEIVE_SMS)) {
                //register for SMS will happen in onResume().
            }
            trackEvent(TrackingAware.FORGOT_PASSWORD_SHOWN, null);
        }
        initializeChangePassword(emailId.trim());
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_forgot_password;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastForSMS();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getCurrentActivity() != null && otpEditTxt != null) {
            BaseActivity.hideKeyboard(getCurrentActivity(), otpEditTxt);
        }
        unregisterBroadcastForSMS();
    }

    private void initializeChangePassword(final String emailId) {

        TextView emailIdTxtView = (TextView) findViewById(R.id.txt_email_id);
        emailIdTxtView.setText(emailId);
        emailIdTxtView.setTypeface(faceRobotoRegular);

        otpEditTxt = (EditText) findViewById(R.id.edit_text_otp);
        otpEditTxt.setTypeface(faceRobotoRegular);
        otpEditTxt.addTextChangedListener(OTPeditextWatcher);

        final OnCompoundDrawableClickListener passWdVisibilityClickListener =
                new OnCompoundDrawableClickListener(OnCompoundDrawableClickListener.DRAWABLE_RIGHT) {
                    @Override
                    public void onRightDrawableClicked() {
                        mIsPasswordVisible = !mIsPasswordVisible;
                        togglePasswordView(newPwdEditText, mIsPasswordVisible);
                        togglePasswordView(confirmPwdEditText, mIsPasswordVisible);
                    }

                    @Override
                    public void onLeftDrawableClicked() {

                    }
                };
        newPwdEditText = (EditText) findViewById(R.id.new_pass_word_edit_txt);
        newPwdEditText.setTypeface(faceRobotoRegular);
        newPwdEditText.setOnTouchListener(passWdVisibilityClickListener);

        confirmPwdEditText = (EditText) findViewById(R.id.confirm_pwd_edit_txt);
        confirmPwdEditText.setTypeface(faceRobotoRegular);
        confirmPwdEditText.setOnTouchListener(passWdVisibilityClickListener);

        txtUpdatePassword = (TextView) findViewById(R.id.txt_update_password);
        txtUpdatePassword.setTypeface(faceRobotoRegular);

        BaseActivity.showKeyboard(otpEditTxt);

        txtUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnUpdateBtnClick(emailId);
            }
        });

        TextView txtResendCode = (TextView) findViewById(R.id.txtResendCode);
        txtResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOtp(emailId);
            }
        });
        /**disabling the passwords fields if the OTP has not been entered*/
        handleEditTextnUpdateViewState(false);
    }

    private void requestOtp(final String email) {
        hideKeyboard(getCurrentActivity(), confirmPwdEditText);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<OtpResponse>> call = bigBasketApiService.getForgotPasswordOtp(
                getCurrentScreenName(), email);
        call.enqueue(new BBNetworkCallback<ApiResponse<OtpResponse>>(this) {
            @Override
            public void onSuccess(ApiResponse<OtpResponse> getForgotPasswordApiResponse) {
                if (getForgotPasswordApiResponse.status == 0) {
                    showToast(getString(R.string.resendOtpMsg));
                } else {
                    logForgotPasswordFailure(getForgotPasswordApiResponse.message);
                    handler.sendEmptyMessage(getForgotPasswordApiResponse.status,
                            getForgotPasswordApiResponse.message);
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

            @Override
            public void onFailure(int httpErrorCode, String msg) {
                super.onFailure(httpErrorCode, msg);
                logForgotPasswordFailure(msg);
            }
        });
    }

    private void logForgotPasswordFailure(String reason) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.FAILURE_REASON, reason);
        trackEvent(TrackingAware.FORGOT_PASSWORD_FAILED, eventAttribs);
    }

    private void OnUpdateBtnClick(String emailId) {
        hideKeyboard(getCurrentActivity(), confirmPwdEditText);
        TextInputLayout textInputOtp = (TextInputLayout) findViewById(R.id.text_input_otp);
        TextInputLayout textInputNewPasswd = (TextInputLayout) findViewById(R.id.text_input_new_passwd);
        TextInputLayout textInputConfirmPasswd = (TextInputLayout) findViewById(R.id.text_input_confirm_passwd);

        UIUtil.resetFormInputField(textInputOtp);
        UIUtil.resetFormInputField(textInputNewPasswd);
        UIUtil.resetFormInputField(textInputConfirmPasswd);

        String otp = otpEditTxt.getText().toString();
        String newPassword = newPwdEditText.getText().toString();
        String confPassword = confirmPwdEditText.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(otp)) {
            cancel = true;
            focusView = otpEditTxt;
            UIUtil.reportFormInputFieldError(textInputOtp, getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(newPassword)) {
            cancel = true;
            if (focusView == null) {
                focusView = newPwdEditText;
                UIUtil.reportFormInputFieldError(textInputNewPasswd, getString(R.string.error_field_required));
            }
        }
        if (TextUtils.isEmpty(confPassword)) {
            cancel = true;
            if (focusView == null) {
                focusView = confirmPwdEditText;
                UIUtil.reportFormInputFieldError(textInputConfirmPasswd, getString(R.string.error_field_required));
            }
        }

        if (newPassword.length() < 6 && !cancel) {
            cancel = true;
            focusView = newPwdEditText;
            UIUtil.reportFormInputFieldError(textInputNewPasswd, getString(R.string.psswordMst6Digit));
        }

        if (confPassword.length() < 6 && !cancel) {
            cancel = true;
            focusView = confirmPwdEditText;
            UIUtil.reportFormInputFieldError(textInputConfirmPasswd, getString(R.string.psswordMst6Digit));
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        if (!newPassword.equals(confPassword)) {
            newPwdEditText.requestFocus();
            showAlertDialog(getString(R.string.app_name), getString(R.string.oldNewPasswordNotMatch));
            return;
        }
        updatePassword(otp, emailId, newPassword);
    }

    private void updatePassword(String otp, String email, final String newPassword) {
        if (!DataUtil.isInternetAvailable(this)) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse> call = bigBasketApiService.updatePasswordWithOtp(getCurrentScreenName(), otp, email, newPassword);
        call.enqueue(new BBNetworkCallback<ApiResponse>(this) {
            @Override
            public void onSuccess(ApiResponse forgotPasswordResponse) {
                if (forgotPasswordResponse.status == 0) {
                    onChangePasswordSuccessResponse(newPassword);
                } else {
                    onChangePasswordErrorResponse(forgotPasswordResponse.message);
                    handler.sendEmptyMessage(forgotPasswordResponse.status,
                            forgotPasswordResponse.message);
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
        });
    }

    private void onChangePasswordSuccessResponse(String newPassword) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains(Constants.PASSWD_PREF)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PASSWD_PREF, newPassword);
            editor.commit();
        }
        showToast(getString(R.string.passwordUpdated));
        finish();

    }

    private void logForgotPasswordErrorEvent(String errorMsg, String eventName) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.FAILURE_REASON, errorMsg);
        trackEvent(eventName, eventAttribs);
    }

    private void onChangePasswordErrorResponse(String errorMsg) {
        logForgotPasswordErrorEvent(errorMsg, TrackingAware.FORGOT_PASSWORD_FAILED);
        otpEditTxt.setText("");
        newPwdEditText.setText("");
        confirmPwdEditText.setText("");
        otpEditTxt.requestFocus();
        BaseActivity.showKeyboard(otpEditTxt);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_FORGOT_PASSWORD_SCREEN;
    }

    private void handleEditTextnUpdateViewState(boolean enable) {
        if (enable) {
            newPwdEditText.setEnabled(true);
            confirmPwdEditText.setEnabled(true);
        } else {
            newPwdEditText.setEnabled(false);
            confirmPwdEditText.setEnabled(false);
        }
    }

    protected void togglePasswordView(EditText passwordEditText, boolean show) {
        Drawable rightDrawable;
        if (!show) {
            rightDrawable = ContextCompat.getDrawable(getCurrentActivity(),
                    R.drawable.ic_visibility_white_18dp);
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
        } else {
            rightDrawable = ContextCompat.getDrawable(getCurrentActivity(),
                    R.drawable.ic_visibility_off_white_18dp);
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, String.valueOf(show));
        trackEvent(TrackingAware.SHOW_PASSWORD_ENABLED, eventAttribs);
        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    private void registerBroadcastForSMS() {
        IntentFilter intentFilter;
        if (otpBroadcastReceiver == null) {
            otpBroadcastReceiver = new OTPBroadcastReceiver(this);
        }
        intentFilter = otpBroadcastReceiver.getIntentFilter();
        registerReceiver(otpBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastForSMS() {
        if (otpBroadcastReceiver == null) return;
        try {
            unregisterReceiver(otpBroadcastReceiver);
            otpBroadcastReceiver = null;
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_RECEIVE_SMS:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.RECEIVE_SMS)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //TODO: Show some indication
                    } else {
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onOTPReceived(String otp) {
        if (TextUtils.isEmpty(otpEditTxt.getText().toString())) {
            otpEditTxt.append(otp);
            handleEditTextnUpdateViewState(true);
            newPwdEditText.requestFocus();
        }
    }
}

