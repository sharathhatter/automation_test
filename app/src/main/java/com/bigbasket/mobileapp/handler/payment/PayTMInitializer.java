package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.os.Bundle;

import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manu on 14/9/15.
 */
public class PayTMInitializer {

    public static PaytmPGService mPaytmPGService = null;
    public static void initiate(HashMap<String, String> paymentParams, Activity ctx,PaytmPaymentTransactionCallback paymentTransactionCallback) {

        mPaytmPGService = PaytmPGService.getStagingService();

        PaytmMerchant Merchant = new PaytmMerchant("https://pguat.paytm.com/merchant-chksum/ChecksumGenerator","https://pguat.paytm.com/merchant-chksum/ValidateChksum");

        //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values

        Map<String, String> paramMap = new HashMap<String, String>();

        //these are mandatory parameters
        paramMap.put("REQUEST_TYPE", "DEFAULT");
        paramMap.put("ORDER_ID", "29");
        paramMap.put("MID", "klbGlV59135347348753");
        paramMap.put("CUST_ID", "CUST123");
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("WEBSITE", "paytm");
        paramMap.put("TXN_AMOUNT", "1");
        paramMap.put("THEME", "merchant");
        PaytmOrder Order = new PaytmOrder(paramMap);
        mPaytmPGService.initialize(Order, Merchant,null);



        mPaytmPGService.startPaymentTransaction(ctx,false,false,paymentTransactionCallback);






    }

}
