package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.util.Constants;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;


public class PayTMInitializer {


    public static void initiate(HashMap<String, String> paymentParams, Activity ctx, PaytmPaymentTransactionCallback paymentTransactionCallback) {

        PaytmPGService paytmPGService = BuildConfig.DEBUG ? PaytmPGService.getStagingService() :
                PaytmPGService.getProductionService();

        String vurl = paymentParams.remove(Constants.PAYTM_HASH_VERIFICATION_URL);
        String gurl = paymentParams.remove(Constants.PAYTM_HASH_GENERATION_URL);
        PaytmMerchant Merchant = new PaytmMerchant(gurl, vurl);
        PaytmOrder Order = new PaytmOrder(paymentParams);
        paytmPGService.initialize(Order, Merchant, null);
        paytmPGService.startPaymentTransaction(ctx, false, false, paymentTransactionCallback);
    }

}
