package com.bigbasket.mobileapp.handler.payment;

import android.os.Bundle;

import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

public class PaytmTxnCallback<T> implements PaytmPaymentTransactionCallback {

    private T ctx;
    private String fullOrderId;
    private String txnId;
    private String potentialOrderId;

    public PaytmTxnCallback(T ctx, String fullOrderId, String txnId, String potentialOrderId) {
        this.ctx = ctx;
        this.fullOrderId = fullOrderId;
        this.txnId = txnId;
        this.potentialOrderId = potentialOrderId;
    }

    @Override
    public void onTransactionSuccess(Bundle bundle) {
        onPaytmSuccess();
    }

    @Override
    public void onTransactionFailure(String s, Bundle bundle) {
        onPaytmFailure();
    }

    @Override
    public void networkNotAvailable() {
        onPaytmFailure();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        onPaytmFailure();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        onPaytmFailure();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        onPaytmFailure();
    }

    private void onPaytmSuccess() {
        new ValidatePaymentHandler<>(ctx, potentialOrderId, txnId, fullOrderId).start();
    }

    private void onPaytmFailure() {
        new ValidatePaymentHandler<>(ctx, potentialOrderId, txnId, fullOrderId).start();
    }
}
