package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.util.Constants;
import com.payu.sdk.PayU;

import java.util.HashMap;

public class PayuInitializer {

    public static void initiate(HashMap<String, String> paymentParams, Activity ctx) {
        Double amount = Double.parseDouble(paymentParams.get(Constants.AMOUNT));

        PayU.merchantCodesHash = paymentParams.remove("merchant_code_hash");
        PayU.paymentHash = paymentParams.remove("payment_hash");
        PayU.vasHash = paymentParams.remove("vas_hash");
        PayU.ibiboCodeHash = paymentParams.remove("mobile_sdk_hash");
        PayU.deleteCardHash = paymentParams.remove("delete_card_hash");
        PayU.getUserCardHash = paymentParams.remove("get_card_hash");
        PayU.editUserCardHash = paymentParams.remove("edit_card_hash");
        PayU.saveUserCardHash = paymentParams.remove("save_card_hash");
        PayU.getInstance(ctx).startPaymentProcess(amount, paymentParams, BuildConfig.DEBUG);
    }
}
