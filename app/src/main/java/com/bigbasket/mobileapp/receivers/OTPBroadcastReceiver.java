package com.bigbasket.mobileapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.bigbasket.mobileapp.interfaces.OnOtpReceivedListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPBroadcastReceiver extends BroadcastReceiver {

    private OnOtpReceivedListener mCallback;

    public OTPBroadcastReceiver(OnOtpReceivedListener callback) {
        mCallback = callback;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter smsOTPintentFilter = new IntentFilter();
        smsOTPintentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsOTPintentFilter.setPriority(2147483647);//setting high priority for dual sim support
        return smsOTPintentFilter;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final Object[] pdusObj = (Object[]) bundle.get("pdus");
            if (pdusObj == null) return;
            for (Object aPduObj : pdusObj) {
                SmsMessage currentMessage;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String msgFormat = bundle.getString("format");
                    // This method is only present in Android M and onwards
                    currentMessage = SmsMessage.createFromPdu((byte[]) aPduObj, msgFormat);
                } else {
                    currentMessage = SmsMessage.createFromPdu((byte[]) aPduObj);
                }
                String phoneNumber;
                if (currentMessage.getDisplayOriginatingAddress() != null) {
                    phoneNumber = currentMessage.getDisplayOriginatingAddress().toUpperCase();
                } else {
                    phoneNumber = "";
                }

                String message;
                if (currentMessage.getDisplayMessageBody() != null) {
                    message = currentMessage.getDisplayMessageBody().toLowerCase();
                } else {
                    message = "";
                }

                /**
                 * checking that the message received is from BigBasket
                 * and it contains the word verification
                 */
                if (phoneNumber.contains("BIG")
                        && (message.contains(" otp "))
                        || message.contains(" verification ") ) {
                    final Pattern p = Pattern.compile("( \\d{4,6}[ .])");
                    final Matcher m = p.matcher(message);
                    if (m.find()) {
                        if (mCallback != null) {
                            String otp = m.group(0).trim();
                            if (otp.endsWith(".")) {
                                otp = otp.substring(0, otp.length() - 1);
                            }
                            mCallback.onOTPReceived(otp);
                        }
                    }
                }
            }
        }
    }
}
