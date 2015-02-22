package com.bigbasket.mobileapp.fragment.account;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;


public class OTPValidationDialogFragment extends AbstractDialogFragment {

    private TextView txtErrorValidateNumber, txtResendNumber;

    public OTPValidationDialogFragment() {
    }

    public static OTPValidationDialogFragment newInstance() {
        return new OTPValidationDialogFragment();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.uiv3_otp_dialog, null);

        FontHolder fontHolder = FontHolder.getInstance(getActivity());
        TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        txtDialogTitle.setTypeface(fontHolder.getFaceRobotoRegular(), Typeface.BOLD);
        TextView txtHeaderMsg = (TextView) view.findViewById(R.id.txtHeaderMsg);
        final EditText editTextMobileCode = (EditText) view.findViewById(R.id.editTextMobileCode);
        editTextMobileCode.setTypeface(fontHolder.getFaceRobotoRegular());
        editTextMobileCode.requestFocus();
        txtHeaderMsg.setTypeface(fontHolder.getFaceRobotoRegular());
        BaseActivity.showKeyboard(editTextMobileCode);
        txtErrorValidateNumber = (TextView) view.findViewById(R.id.txtErrorValidateNumber);
        txtErrorValidateNumber.setTypeface(fontHolder.getFaceRobotoRegular());
        txtResendNumber = (TextView) view.findViewById(R.id.txtResendNumber);
        txtResendNumber.setTypeface(fontHolder.getFaceRobotoRegular());
        TextView txtResendCode = (TextView) view.findViewById(R.id.txtResendCode);
        txtResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtErrorValidateNumber.setVisibility(View.GONE);
                txtResendNumber.setVisibility(View.VISIBLE);
                resendOrConfirmOTP(null);
            }
        });

        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getActivity())
                .autoDismiss(false)
                .cancelable(false)
                .customView(view, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (!TextUtils.isEmpty(editTextMobileCode.getText().toString())) {
                            if (editTextMobileCode.getText() != null && editTextMobileCode.getText().toString().length() > 0) {
                                resendOrConfirmOTP(editTextMobileCode.getText().toString());
                            } else {
                                txtErrorValidateNumber.setText(getResources().getString(R.string.otpCodeErrorMsg));
                                txtErrorValidateNumber.setVisibility(View.VISIBLE);
                                txtResendNumber.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        if (getDialog().isShowing()) {
                            getDialog().dismiss();
                        }
                    }
                });
        Dialog dialog = builder.build();
        ((TrackingAware) getActivity()).trackEvent(TrackingAware.OTP_DIALOG_SHOWN, null);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void showErrorText(String errorMsg) {
        txtErrorValidateNumber.setText(errorMsg != null ? errorMsg : getResources().getString(R.string.otpCodeErrorMsg));
        txtErrorValidateNumber.setVisibility(View.VISIBLE);
        txtResendNumber.setVisibility(View.GONE);
    }

    public void resendOrConfirmOTP(String otp) {
        if (getTargetFragment() != null) {
            ((OtpDialogAware) getTargetFragment()).validateOtp(otp);
        } else if (getActivity() != null) {
            ((OtpDialogAware) getActivity()).validateOtp(otp);
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.OTP_SCREEN;
    }
}
