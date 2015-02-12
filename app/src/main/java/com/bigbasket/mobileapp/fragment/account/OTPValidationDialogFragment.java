package com.bigbasket.mobileapp.fragment.account;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;

/**
 * Created by jugal on 24/9/14.
 */
public class OTPValidationDialogFragment extends DialogFragment {

    private TextView txtErrorValidateNumber, txtResendNumber;

    public OTPValidationDialogFragment() {
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
            txtDialogTitle.setTypeface(BaseActivity.faceRobotoRegular, Typeface.BOLD);
            TextView txtHeaderMsg = (TextView) view.findViewById(R.id.txtHeaderMsg);
            final EditText editTextMobileCode = (EditText) view.findViewById(R.id.editTextMobileCode);
            editTextMobileCode.setTypeface(BaseActivity.faceRobotoRegular);
            editTextMobileCode.requestFocus();
            txtHeaderMsg.setTypeface(BaseActivity.faceRobotoRegular);
            BaseActivity.showKeyboard(editTextMobileCode);
            txtErrorValidateNumber = (TextView) view.findViewById(R.id.txtErrorValidateNumber);
            txtErrorValidateNumber.setTypeface(BaseActivity.faceRobotoRegular);
            txtResendNumber = (TextView) view.findViewById(R.id.txtResendNumber);
            txtResendNumber.setTypeface(BaseActivity.faceRobotoRegular);
            TextView txtResendCode = (TextView) view.findViewById(R.id.txtResendCode);
            txtResendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtErrorValidateNumber.setVisibility(View.GONE);
                    txtResendNumber.setVisibility(View.VISIBLE);
                    resendOrConfirmOTP(null);
                }
            });
            final TextView btnConfirm = (TextView) view.findViewById(R.id.txtConfirm);

            btnConfirm.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        btnConfirm.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        btnConfirm.setBackgroundColor(getResources().getColor(R.color.white));
                        if (TextUtils.isEmpty(editTextMobileCode.getText().toString())) {
                        } else if (editTextMobileCode.getText() != null && editTextMobileCode.getText().toString().length() > 0) {
                            resendOrConfirmOTP(editTextMobileCode.getText().toString());
                        } else {
                            txtErrorValidateNumber.setText(getResources().getString(R.string.otpCodeErrorMsg));
                            txtErrorValidateNumber.setVisibility(View.VISIBLE);
                            txtResendNumber.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }
            });


            final TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
            txtCancel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        txtCancel.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        txtCancel.setBackgroundColor(getResources().getColor(R.color.white));
                        if (getDialog().isShowing())
                            getDialog().dismiss();
                    }
                    return true;
                }
            });

        }
    }

    public void showErrorText(String errorMsg) {
        txtErrorValidateNumber.setText(errorMsg != null ? errorMsg : getResources().getString(R.string.otpCodeErrorMsg));
        txtErrorValidateNumber.setVisibility(View.VISIBLE);
        txtResendNumber.setVisibility(View.GONE);
    }

    public void resendOrConfirmOTP(String otp) {
    }

}
