package com.bigbasket.mobileapp.handler.payment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.util.Constants;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;

public class PaytmTxnCallback implements PaytmPaymentTransactionCallback {

    @Nullable
    private String orderId;
    @Nullable
    private String potentialOrderId;
    private boolean isPayNow;
    private boolean isWallet;
    private String txnId;

    public PaytmTxnCallback(@Nullable String orderId, @Nullable String potentialOrderId,
                            String txnId, boolean isPayNow, boolean isWallet) {
        this.orderId = orderId;
        this.potentialOrderId = potentialOrderId;
        this.isPayNow = isPayNow;
        this.isWallet = isWallet;
        this.txnId = txnId;
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
        onPaytmCallback(false, null);
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        onPaytmCallback(false, null);
    }

    @Override
    public void someUIErrorOccurred(String s) {
        onPaytmCallback(false, null);
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        onPaytmCallback(false, null);
    }

    @Override
    public void onBackPressedCancelTransaction() {
        onPaytmCallback(false, null);
    }

    private void onPaytmCallback(boolean status, @Nullable Bundle bundle) {
        HashMap<String, String> paramsMap = new HashMap<>();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object val = bundle.get(key);
                if (val instanceof String) {
                    paramsMap.put(key, val.toString());
                }
            }
        }

        String txnId = paramsMap.get("ORDERID");
        if (TextUtils.isEmpty(txnId)) {
            txnId = paramsMap.get("ORDER_ID");
        }
        if (TextUtils.isEmpty(txnId)) {
            txnId = this.txnId;
            paramsMap.put("ORDER_ID", txnId);
        }

        ValidatePaymentRequest validatePaymentRequest =
                new ValidatePaymentRequest(txnId, orderId, potentialOrderId, Constants.PAYTM_WALLET);
        validatePaymentRequest.setIsPayNow(isPayNow);
        validatePaymentRequest.setIsWallet(isWallet);
        PaytmResponseHolder.setPaytmResponse(status, validatePaymentRequest, paramsMap);
    }
}
