package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.os.AsyncTask;

import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.PowerPayPostParams;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;

public class PowerPayInitializer {

    public static void initiate(Activity activity, PowerPayPostParams powerPayPostParams) {
        new PowerPayTriggerAsyncTask(activity).execute(powerPayPostParams);
    }

    private static void startHDFCPowerPay(PowerPayPostParams powerPayPostParams,
                                          Activity activity) {
        WPayInitRequest wPayInitRequest = new WPayInitRequest();

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTxnAmount(powerPayPostParams.getFormattedAmount());
        transactionInfo.setTxnCurrency(powerPayPostParams.getCurrency());
        transactionInfo.setSupportedPaymentType(powerPayPostParams.getPaymentChoices());
        transactionInfo.setTxnDesc(powerPayPostParams.getTxnDesc());
        transactionInfo.setMerTxnId(powerPayPostParams.getTxnId());
        if (powerPayPostParams.getAppData() != null) {
            transactionInfo.setMerAppData(powerPayPostParams.getAppData());
        }

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerAppId(powerPayPostParams.getMerchantAppId());
        merchantInfo.setMerId(powerPayPostParams.getMerchantId());
        merchantInfo.setMerCountryCode(powerPayPostParams.getCountryCode());

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustEmail(powerPayPostParams.getEmail());
        customerInfo.setCustDob(powerPayPostParams.getDob());
        customerInfo.setCustName(powerPayPostParams.getName());
        customerInfo.setCustMobile(powerPayPostParams.getMobile());

        wPayInitRequest.setTransactionInfo(transactionInfo);
        wPayInitRequest.setMerchantInfo(merchantInfo);
        wPayInitRequest.setCustomerInfo(customerInfo);

        wPayInitRequest.setMsgHash(powerPayPostParams.getMsgHash());
        WibmoSDK.startForInApp(activity, wPayInitRequest);
    }

    private static class PowerPayTriggerAsyncTask extends AsyncTask<PowerPayPostParams, Integer, PowerPayPostParams> {

        private Activity activity;

        public PowerPayTriggerAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            ((ProgressIndicationAware) activity).showProgressDialog("Please wait...");
        }

        @Override
        protected PowerPayPostParams doInBackground(PowerPayPostParams... params) {
            if (((CancelableAware) activity).isSuspended()) return null;
            PowerPayPostParams powerPayPostParams = params[0];
            WibmoSDK.setWibmoIntentActionPackage(powerPayPostParams.getPkgName());
            WibmoSDKConfig.setWibmoDomain(powerPayPostParams.getServerUrl());
            WibmoSDK.init(activity.getApplicationContext());
            return powerPayPostParams;
        }

        @Override
        protected void onPostExecute(PowerPayPostParams powerPayPostParams) {
            if (((CancelableAware) activity).isSuspended() ||
                    powerPayPostParams == null) return;
            ((ProgressIndicationAware) activity).hideProgressDialog();
            startHDFCPowerPay(powerPayPostParams, activity);
        }
    }

}
