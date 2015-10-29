package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.handler.payment.PaytmTxnCallback;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.util.Constants;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;

import java.util.HashMap;

public class PaytmPayment {

    public void startPaymentGateway(HashMap<String, String> paymentParams, Activity ctx,
                                    @Nullable String potentialOrderId,
                                    @Nullable String fullOrderId,
                                    boolean isPayNow, boolean isFundWallet) {

        if (ctx instanceof PaymentTxnInfoAware) {
            ((PaymentTxnInfoAware) ctx).setTxnId(paymentParams.get("ORDER_ID"));
        }
        PaytmPGService paytmPGService = BuildConfig.DEBUG ? PaytmPGService.getStagingService() :
                PaytmPGService.getProductionService();

        String vurl = paymentParams.remove(Constants.PAYTM_HASH_VERIFICATION_URL);
        String gurl = paymentParams.remove(Constants.PAYTM_HASH_GENERATION_URL);
        PaytmMerchant merchant = new PaytmMerchant(gurl, vurl);
        PaytmOrder order = new PaytmOrder(paymentParams);
        paytmPGService.initialize(order, merchant, null);
        paytmPGService.startPaymentTransaction(ctx, false, false,
                new PaytmTxnCallback<>(ctx, fullOrderId, potentialOrderId, isPayNow, isFundWallet));
    }
}
