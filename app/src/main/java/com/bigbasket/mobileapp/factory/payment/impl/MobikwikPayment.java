package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class MobikwikPayment {

    public void startPaymentGateway(HashMap<String, String> paymentParams, Activity ctx) {
        Intent walletIntent = new Intent(Constants.MOBIKWIKSDK);
        walletIntent.setPackage(ctx.getPackageName());
        walletIntent.setType("text/plain");
        for (Map.Entry<String, String> entry : paymentParams.entrySet()) {
            walletIntent.putExtra(entry.getKey(), entry.getValue());
        }
        String sdkMode = ((ActivityAware) ctx).getCurrentActivity().getResources().getString(R.string.mobikwik_mode);
        walletIntent.putExtra(Constants.MOBIKWIK_MODE, sdkMode);
        String sdkSign = ((ActivityAware) ctx).getCurrentActivity().getResources().getString(R.string.mobikwik_sdk_sign);
        walletIntent.putExtra(Constants.MOBIKWIK_SDK_SIGN, sdkSign);

        walletIntent.putExtra(Constants.MOBIKWIK_PAYMENT_OPTION, Constants.MOBIKWIK_PAYMENT_MW);
        walletIntent.putExtra(Constants.MOBIKWIK_REDIRECT_ANDROID_CLASS_NAME,
                Constants.MOBIKWIK_RESPONSE_HANDLER_CLASS);
        ctx.startActivity(walletIntent);
    }
}
