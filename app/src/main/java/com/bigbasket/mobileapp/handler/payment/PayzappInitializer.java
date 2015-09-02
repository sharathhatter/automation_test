package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.os.AsyncTask;

import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;

public class PayzappInitializer {

    public static void initiate(Activity activity, PayzappPostParams payzappPostParams) {
        new PayzappTriggerAsyncTask(activity).execute(payzappPostParams);
    }

    private static void startHDFCPayzapp(PayzappPostParams payzappPostParams,
                                         Activity activity) {
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

    private static class PayzappTriggerAsyncTask extends AsyncTask<PayzappPostParams, Integer, PayzappPostParams> {

        private Activity activity;

        public PayzappTriggerAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            ((ProgressIndicationAware) activity).showProgressDialog("Please wait...");
        }

        @Override
        protected PayzappPostParams doInBackground(PayzappPostParams... params) {
            if (((CancelableAware) activity).isSuspended()) return null;
            PayzappPostParams payzappPostParams = params[0];
            WibmoSDK.setWibmoIntentActionPackage(payzappPostParams.getPkgName());
            WibmoSDKConfig.setWibmoDomain(payzappPostParams.getServerUrl());
            WibmoSDK.init(activity.getApplicationContext());
            return payzappPostParams;
        }

        @Override
        protected void onPostExecute(PayzappPostParams payzappPostParams) {
            if (((CancelableAware) activity).isSuspended() ||
                    payzappPostParams == null) return;
            ((ProgressIndicationAware) activity).hideProgressDialog();
            startHDFCPayzapp(payzappPostParams, activity);
        }
    }

}
