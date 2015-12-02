package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;

import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;

public class HDFCPayzappPayment {

    public static void startHDFCPayzapp(PayzappPostParams payzappPostParams,
                                        Activity activity) {
        if (payzappPostParams == null || activity == null) {
            throw new IllegalArgumentException("Illegal arguments, activity: " + activity
                    + ", payzappPostParams: " + payzappPostParams);
        }
        if (activity instanceof PaymentTxnInfoAware) {
            ((PaymentTxnInfoAware) activity).setTxnId(payzappPostParams.getTxnId());
        }
        WPayInitRequest wPayInitRequest = new WPayInitRequest();

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTxnAmount(payzappPostParams.getFormattedAmount());
        transactionInfo.setTxnCurrency(payzappPostParams.getCurrency());
        transactionInfo.setSupportedPaymentType(payzappPostParams.getPaymentChoices());
        transactionInfo.setTxnDesc(payzappPostParams.getTxnDesc());
        transactionInfo.setMerTxnId(payzappPostParams.getTxnId());
        if (payzappPostParams.getAppData() != null) {
            transactionInfo.setMerAppData(payzappPostParams.getAppData());
        }

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerAppId(payzappPostParams.getMerchantAppId());
        merchantInfo.setMerId(payzappPostParams.getMerchantId());
        merchantInfo.setMerCountryCode(payzappPostParams.getCountryCode());

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustEmail(payzappPostParams.getEmail());
        customerInfo.setCustDob(payzappPostParams.getDob());
        customerInfo.setCustName(payzappPostParams.getName());
        customerInfo.setCustMobile(payzappPostParams.getMobile());

        wPayInitRequest.setTransactionInfo(transactionInfo);
        wPayInitRequest.setMerchantInfo(merchantInfo);
        wPayInitRequest.setCustomerInfo(customerInfo);

        wPayInitRequest.setMsgHash(payzappPostParams.getMsgHash());
        WibmoSDK.startForInApp(activity, wPayInitRequest);
    }
}
