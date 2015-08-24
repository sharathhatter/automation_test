package com.bigbasket.mobileapp.fragment.account;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;

public class OTPValidationDialogFragment extends AbstractDialogFragment {

    private TextView txtErrorValidateNumber, txtResendNumber;
    private EditText editTextMobileCode;

    public OTPValidationDialogFragment() {
    }

    public EditText getEditTextMobileCode() {
        return editTextMobileCode;
    }


    public static OTPValidationDialogFragment newInstance() {
        return new OTPValidationDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_otp_dialog, container, false);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
            txtDialogTitle.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular(), Typeface.BOLD);
            TextView txtHeaderMsg = (TextView) view.findViewById(R.id.txtHeaderMsg);
            editTextMobileCode = (EditText) view.findViewById(R.id.editTextMobileCode);
            editTextMobileCode.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
            editTextMobileCode.requestFocus();
            txtHeaderMsg.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
            BaseActivity.showKeyboard(editTextMobileCode);
            txtErrorValidateNumber = (TextView) view.findViewById(R.id.txtErrorValidateNumber);
            txtErrorValidateNumber.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
            txtResendNumber = (TextView) view.findViewById(R.id.txtResendNumber);
            txtResendNumber.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
            TextView txtResendCode = (TextView) view.findViewById(R.id.txtResendCode);
            txtResendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextMobileCode.setText("");
                    txtErrorValidateNumber.setVisibility(View.GONE);
                    txtResendNumber.setVisibility(View.VISIBLE);
                    resendOrConfirmOTP(null);
                }
            });

            final TextView btnConfirm = (TextView) view.findViewById(R.id.txtConfirm);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(editTextMobileCode.getText().toString())) {
                    } else if (editTextMobileCode.getText() != null && editTextMobileCode.getText().toString().length() > 0) {
                        resendOrConfirmOTP(editTextMobileCode.getText().toString());
                    } else {
                        txtErrorValidateNumber.setText(getResources().getString(R.string.otpCodeErrorMsg));
                        txtErrorValidateNumber.setVisibility(View.VISIBLE);
                        txtResendNumber.setVisibility(View.GONE);
                    }
                }
            });


            final TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
            txtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtCancel.setBackgroundColor(getResources().getColor(R.color.white));
                    BaseActivity.hideKeyboard((BaseActivity) getActivity(), editTextMobileCode);
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                }
            });
        }
        ((TrackingAware) getActivity()).trackEvent(TrackingAware.OTP_DIALOG_SHOWN, null);
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
