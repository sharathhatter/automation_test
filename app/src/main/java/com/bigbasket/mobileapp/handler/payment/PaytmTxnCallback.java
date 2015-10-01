package com.bigbasket.mobileapp.handler.payment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.util.Constants;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;

public class PaytmTxnCallback<T> implements PaytmPaymentTransactionCallback {

    private T ctx;
    @Nullable
    private String orderId;
    @Nullable
    private String potentialOrderId;
    private boolean isPayNow;
    private boolean isWallet;

    public PaytmTxnCallback(T ctx, @Nullable String orderId, @Nullable String potentialOrderId,
                            boolean isPayNow, boolean isWallet) {
        this.ctx = ctx;
        this.orderId = orderId;
        this.potentialOrderId = potentialOrderId;
        this.isPayNow = isPayNow;
        this.isWallet = isWallet;
    }

    @Override
    public void onTransactionSuccess(Bundle bundle) {
        onPaytmCallback(true, bundle);
    }

    @Override
    public void onTransactionFailure(String s, Bundle bundle) {
        onPaytmCallback(false, bundle);
    }

    @Override
    public void networkNotAvailable() {
        ((HandlerAware) ctx).getHandler().sendOfflineError();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        ((ApiErrorAware) ctx).showApiErrorDialog("Error!", s);
    }

    @Override
    public void someUIErrorOccurred(String s) {
        ((ApiErrorAware) ctx).showApiErrorDialog("Error!", s);
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        ((ApiErrorAware) ctx).showApiErrorDialog("Error!", inErrorMessage);
    }

    private void onPaytmCallback(boolean status, Bundle bundle) {
        HashMap<String, String> paramsMap = new HashMap<>();
        for (String key : bundle.keySet()) {
            Object val = bundle.get(key);
            if (val instanceof String) {
                paramsMap.put(key, val.toString());
            }
        }

        String txnId = paramsMap.get("ORDERID");
        if (TextUtils.isEmpty(txnId)) {
            paramsMap.get("ORDER_ID");
        }
        new PostPaymentHandler<>(ctx, potentialOrderId, Constants.PAYTM_WALLET, status, orderId)
                .setPayNow(isPayNow)
                .isWallet(isWallet)
                .setTxnId(txnId)
                .setPayTmParams(paramsMap)
                .start();
    }
}
