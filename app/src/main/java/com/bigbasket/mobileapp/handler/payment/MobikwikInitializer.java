package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.payu.sdk.PayU;

import org.apache.http.protocol.HTTP;

import java.util.HashMap;

/**
 * Created by jugal on 19/8/15.
 */
public class MobikwikInitializer {

    public static void initiate(HashMap<String, String> paymentParams, Activity ctx) {
        //String amount = UIUtil.round(Double.parseDouble(paymentParams.get(Constants.MOBIKWIK_AMOUNT)));
        Intent walletIntent = new Intent(Constants.MOBIKWIKSDK);
        walletIntent.setPackage(ctx.getPackageName());
        walletIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        int orderID = Integer.parseInt(paymentParams.get(Constants.MOBIKWIK_ORDER_ID));
        orderID = orderID * 1000; //todo remove this
        walletIntent.putExtra(Constants.MOBIKWIK_ORDER_ID, String.valueOf(orderID));
        walletIntent.putExtra(Constants.MOBIKWIK_DEBIT_WALLET, paymentParams.get(Constants.MOBIKWIK_DEBIT_WALLET));
        walletIntent.putExtra(Constants.MOBIKWIK_AMOUNT, paymentParams.get(Constants.MOBIKWIK_AMOUNT));
        walletIntent.putExtra(Constants.MOBIKWIK_EMAIL, paymentParams.get(Constants.MOBIKWIK_EMAIL));
        walletIntent.putExtra(Constants.MOBIKWIK_CELL, paymentParams.get(Constants.MOBIKWIK_CELL));
        walletIntent.putExtra(Constants.MOBIKWIK_MID, paymentParams.get(Constants.MOBIKWIK_MID));
        walletIntent.putExtra(Constants.MOBIKWIK_MERCHANT_NAME, paymentParams.get(Constants.MOBIKWIK_MERCHANT_NAME));
        walletIntent.putExtra(Constants.MOBIKWIK_MODE, Constants.MOBIKWIK_MODE_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_SDK_SIGN, Constants.MOBIKWIK_SDK_SIGN_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_PAYMENT_OPTION, Constants.MOBIKWIK_PAYMENT_MW);
        walletIntent.putExtra(Constants.MOBIKWIK_REDIRECT_ANDROID_CLASS_NAME,
                Constants.MOBIKWIK_RESPONSE_HANDLER_CLASS);
        walletIntent.putExtra(Constants.MOBIKWIK_CHECKSUM_URL,
                paymentParams.get(Constants.MOBIKWIK_CHECKSUM_URL));
        walletIntent.putExtra(Constants.MOBIKWIK_CALL_BACK_URL,
                paymentParams.get(Constants.MOBIKWIK_CALL_BACK_URL));
        ctx.startActivity(walletIntent);
    }
}
