package com.bigbasket.mobileapp.fragment.account;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
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
import com.crashlytics.android.Crashlytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPValidationDialogFragment extends AbstractDialogFragment {

    private TextView txtErrorValidateNumber, txtResendNumber;
    private EditText editTextMobileCode;
    private BroadcastReceiver broadcastReceiver;

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
            });


            final TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
            txtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtCancel.setBackgroundColor(getResources().getColor(R.color.white));
                    BaseActivity.hideKeyboard(getActivity(), editTextMobileCode);
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                }
            });
        }
        registerBroadcastForSMS();
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

    private void registerBroadcastForSMS() {
        IntentFilter smsOTPintentFilter = new IntentFilter();
        smsOTPintentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsOTPintentFilter.setPriority(2147483647);//setting high priority for dual sim support
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    if (pdusObj == null) return;
                    for (Object aPduObj : pdusObj) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPduObj);
                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();

                        /**
                         * checking that the message received is from BigBasket
                         * and it contains the word verification
                         */
                        if ((phoneNumber.toUpperCase().contains("BIG") &&
                                (message.toLowerCase().contains("verification")))) {
                            final Pattern p = Pattern.compile("(\\d{4})");
                            final Matcher m = p.matcher(message);
                            if (m.find() && this!= null
                                    && isVisible()) {
                                resendOrConfirmOTP(m.group(0));
                            }
                        }
                    }
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver, smsOTPintentFilter);
    }

    private void unregisterBroadcastForSMS() {
        if (broadcastReceiver == null) return;
        try {
           getActivity().unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onDestroyView() {
        unregisterBroadcastForSMS();
        super.onDestroyView();
    }
}
