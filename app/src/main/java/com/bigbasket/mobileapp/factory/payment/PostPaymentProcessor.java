package com.bigbasket.mobileapp.factory.payment;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.handler.payment.SendPaymentResponseTask;
import com.bigbasket.mobileapp.handler.payment.ValidatePaymentHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;

import java.util.HashMap;

public class PostPaymentProcessor<T extends AppOperationAware> {
    private T ctx;
    private String txnId;
    @Nullable
    private String potentialOrderId;
    @Nullable
    private String orderId;
    private boolean isPayNow;
    private boolean isFundWallet;

    public PostPaymentProcessor(T ctx, String txnId) {
        this.ctx = ctx;
        this.txnId = txnId;
    }

    public PostPaymentProcessor withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public PostPaymentProcessor withPotentialOrderId(String potentialOrderId) {
        this.potentialOrderId = potentialOrderId;
        return this;
    }

    public PostPaymentProcessor withIsPayNow(boolean isPayNow) {
        this.isPayNow = isPayNow;
        return this;
    }

    public PostPaymentProcessor withIsFundWallet(boolean isFundWallet) {
        this.isFundWallet = isFundWallet;
        return this;
    }

    public void processPayzapp(Intent data, int resultCode, String finalTotal) {
        if (resultCode == Activity.RESULT_OK) {
            WPayResponse res = WibmoSDK.processInAppResponseWPay(data);
            String pgTxnId = res.getWibmoTxnId();
            String dataPickupCode = res.getDataPickUpCode();
            validateHdfcPayzappResponse(pgTxnId, dataPickupCode, txnId, finalTotal);
        } else {
            if (data != null) {
                String resCode = data.getStringExtra("ResCode");
                String resDesc = data.getStringExtra("ResDesc");
                communicateHdfcPayzappResponseFailure(resCode, resDesc, finalTotal);
            } else {
                communicateHdfcPayzappResponseFailure(null, null, finalTotal);
            }
        }
    }

    public void processPaytm(boolean status, HashMap<String, String> paramsMap) {
        new SendPaymentResponseTask<>(ctx, potentialOrderId, Constants.PAYTM_WALLET, status, orderId)
                .setPayNow(isPayNow)
                .isWallet(isFundWallet)
                .setTxnId(txnId)
                .setPayTmParams(paramsMap)
                .start();
    }

    public void processPayment() {
        new ValidatePaymentHandler<>(ctx, potentialOrderId, txnId, orderId).start();
    }

    private void validateHdfcPayzappResponse(String pgTxnId, String dataPickupCode,
                                             String txnId, String finalTotal) {
        new SendPaymentResponseTask<>(ctx, potentialOrderId, Constants.HDFC_POWER_PAY,
                true, orderId)
                .setDataPickupCode(dataPickupCode)
                .setPgTxnId(pgTxnId)
                .setTxnId(txnId)
                .setAmount(finalTotal)
                .isWallet(isFundWallet)
                .setPayNow(isPayNow)
                .start();
    }

    private void communicateHdfcPayzappResponseFailure(String resCode, String resDesc, String finalTotal) {
        new SendPaymentResponseTask<>(ctx, potentialOrderId, Constants.HDFC_POWER_PAY,
                false, orderId)
                .setErrResCode(resCode)
                .setErrResDesc(resDesc)
                .setTxnId(txnId)
                .setAmount(finalTotal)
                .isWallet(isFundWallet)
                .setPayNow(isPayNow)
                .start();
    }
}
