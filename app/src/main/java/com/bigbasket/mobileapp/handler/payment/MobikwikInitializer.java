package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jugal on 19/8/15.
 */
public class MobikwikInitializer {

    public static void initiate(HashMap<String, String> paymentParams, Activity ctx) {
        Intent walletIntent = new Intent(Constants.MOBIKWIKSDK);
        walletIntent.setPackage(ctx.getPackageName());
        walletIntent.setType("text/plain");
        for (Map.Entry<String, String> entry : paymentParams.entrySet()) {
            walletIntent.putExtra(entry.getKey(), entry.getValue());
        }
        walletIntent.putExtra("debitwallet", "False");
        walletIntent.putExtra(Constants.MOBIKWIK_MODE, Constants.MOBIKWIK_MODE_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_SDK_SIGN, Constants.MOBIKWIK_SDK_SIGN_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_PAYMENT_OPTION, Constants.MOBIKWIK_PAYMENT_MW);
        walletIntent.putExtra(Constants.MOBIKWIK_REDIRECT_ANDROID_CLASS_NAME,
                Constants.MOBIKWIK_RESPONSE_HANDLER_CLASS);
        ctx.startActivity(walletIntent);
    }
}
