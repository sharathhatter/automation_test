package com.bigbasket.mobileapp.fragment.account;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.OnOtpReceivedListener;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.receivers.OTPBroadcastReceiver;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.crashlytics.android.Crashlytics;

public class OTPValidationFragment extends BaseFragment implements OnOtpReceivedListener {

    private TextView mTxtOtpErrorMsg;
    private EditText mEditTextMobileCode;
    private TextView mTxtEnterOtpManually;
    private TextView mTxtHeaderMsg;
    private ProgressBar mProgressBarOtp;
    @Nullable
    private OTPBroadcastReceiver mSmsBroadcastReceiver;

    public OTPValidationFragment() {
    }

    public static OTPValidationFragment newInstance() {
        return new OTPValidationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_otp_dialog, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderOtpValidationView();
        registerBroadcastForSMS();
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return null;
    }

    private void renderOtpValidationView() {
        View base = getView();
        if (base == null || getActivity() == null) return;
        BaseActivity.hideKeyboard(getActivity(), base);
        final Button btnConfirmOtp = (Button) base.findViewById(R.id.btnConfirmOtp);
        btnConfirmOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmOtpButtonClicked();
            }
        });
        btnConfirmOtp.setTypeface(faceRobotoRegular);

        mTxtOtpErrorMsg = (TextView) base.findViewById(R.id.txtOtpErrorMsg);
        mTxtOtpErrorMsg.setTypeface(faceRobotoRegular);
        mTxtOtpErrorMsg.setVisibility(View.GONE);

        mEditTextMobileCode = (EditText) base.findViewById(R.id.editTextMobileCode);
        mEditTextMobileCode.setText("");
        mEditTextMobileCode.setTypeface(faceRobotoRegular);
        mEditTextMobileCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    BaseActivity.hideKeyboard(getActivity(), mEditTextMobileCode);
                    onConfirmOtpButtonClicked();
                }
                return false;
            }
        });


        mProgressBarOtp = (ProgressBar) base.findViewById(R.id.progressBarOtpSmall);

        mTxtEnterOtpManually = (TextView) base.findViewById(R.id.txtEnterOtpManually);
        mTxtEnterOtpManually.setTypeface(faceRobotoRegular);

        mTxtHeaderMsg = (TextView) base.findViewById(R.id.txtHeaderMsg);
        mTxtHeaderMsg.setTypeface(faceRobotoRegular);


        base.findViewById(R.id.btnResendCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResendOtpButtonClicked();
            }
        });
        base.findViewById(R.id.imgCloseOtp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ((TrackingAware) getActivity()).trackEvent(TrackingAware.OTP_DIALOG_SHOWN, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        changeUIForSmsPermission();
    }

    private void changeUIForSmsPermission() {
        View base = getView();
        if (base == null || getCurrentActivity() == null) return;
        boolean hasSmsPermission = getCurrentActivity().hasPermissionGranted(Manifest.permission.RECEIVE_SMS);
        if (hasSmsPermission) {
            mProgressBarOtp.setVisibility(View.VISIBLE);
            mTxtEnterOtpManually.setVisibility(View.VISIBLE);
            mTxtHeaderMsg.setText(R.string.headerMsgForMobileConfirm);
        } else {
            mProgressBarOtp.setVisibility(View.GONE);
            mTxtEnterOtpManually.setVisibility(View.GONE);
            mTxtHeaderMsg.setText(R.string.headerMsgForMobileConfirmWhenPermissionDenied);
        }
    }

    public void dismiss() {
        if (getCurrentActivity() != null && !isSuspended() && mEditTextMobileCode != null) {
            BaseActivity.hideKeyboard(getCurrentActivity(), mEditTextMobileCode);
        }
        finish();
    }

    private void onResendOtpButtonClicked() {
        mTxtOtpErrorMsg.setVisibility(View.GONE);
        mEditTextMobileCode.setText("");
        resendOrConfirmOTP(null);
    }

    private void onConfirmOtpButtonClicked() {
        String otp = mEditTextMobileCode.getText().toString();
        if (TextUtils.isEmpty(otp)) {
            mTxtOtpErrorMsg.setVisibility(View.VISIBLE);
            mTxtOtpErrorMsg.setText(getString(R.string.otpCodeMissing));
            return;
        }
        resendOrConfirmOTP(otp);
    }

    public void showErrorText(String errorMsg) {
        if (mTxtOtpErrorMsg == null) return;
        mTxtOtpErrorMsg.setVisibility(View.VISIBLE);
        mTxtOtpErrorMsg.setText(errorMsg != null ? errorMsg : getResources().getString(R.string.otpCodeErrorMsg));
    }

    public void resendOrConfirmOTP(String otp) {
        if (getTargetFragment() != null) {
            ((OtpDialogAware) getTargetFragment()).validateOtp(otp, otp == null);
        } else if (getActivity() != null) {
            ((OtpDialogAware) getActivity()).validateOtp(otp, otp == null);
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.OTP_SCREEN;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OTPValidationFragment.class.getName();
    }

    private void registerBroadcastForSMS() {
        if(mSmsBroadcastReceiver == null) {
            mSmsBroadcastReceiver = new OTPBroadcastReceiver(this);
        }
        getActivity().registerReceiver(mSmsBroadcastReceiver, OTPBroadcastReceiver.getIntentFilter());
    }

    @Override
    public void onOTPReceived(String otp) {
        resendOrConfirmOTP(otp);
    }

    private void unregisterBroadcastForSMS() {
        if (mSmsBroadcastReceiver == null) return;
        try {
            getActivity().unregisterReceiver(mSmsBroadcastReceiver);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onDestroyView() {
        unregisterBroadcastForSMS();
        super.onDestroyView();
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "OTPValidationFragment";
    }
}
