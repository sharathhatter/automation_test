package com.bigbasket.mobileapp.handler.payment;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.BuildConfig;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.PayUBaseActivity;

import java.util.HashMap;

public class PayuInitializer {

    public static void initiate(HashMap<String, String> paymentParamsMap, Activity ctx) {

        PaymentParams paymentParams = new PaymentParams();
        paymentParams.setKey(paymentParamsMap.get(PayuConstants.KEY));
        paymentParams.setAmount(paymentParamsMap.get(PayuConstants.AMOUNT));
        paymentParams.setProductInfo(paymentParamsMap.get(PayuConstants.PRODUCT_INFO));
        paymentParams.setFirstName(paymentParamsMap.get(PayuConstants.FIRST_NAME));
        paymentParams.setEmail(paymentParamsMap.get(PayuConstants.EMAIL));
        paymentParams.setTxnId(paymentParamsMap.get(PayuConstants.TXNID));
        paymentParams.setSurl(paymentParamsMap.get(PayuConstants.SURL));
        paymentParams.setFurl(paymentParamsMap.get(PayuConstants.FURL));
        paymentParams.setOfferKey(paymentParamsMap.get(PayuConstants.OFFER_KEY));
        paymentParams.setUdf1(paymentParamsMap.get(PayuConstants.UDF1));
        paymentParams.setUdf2(paymentParamsMap.get(PayuConstants.UDF2));
        paymentParams.setUdf3(paymentParamsMap.get(PayuConstants.UDF3));
        paymentParams.setUdf4(paymentParamsMap.get(PayuConstants.UDF4));
        paymentParams.setUdf5(paymentParamsMap.get(PayuConstants.UDF5));

        PayuHashes payuHashes = new PayuHashes();
        payuHashes.setPaymentHash(paymentParamsMap.get("payment_hash"));
        payuHashes.setPaymentHash(paymentParamsMap.get(PayuConstants.HASH));
        payuHashes.setVasForMobileSdkHash(paymentParamsMap.get("vas_hash"));
        payuHashes.setDeleteCardHash(paymentParamsMap.get("delete_card_hash"));
        payuHashes.setEditCardHash(paymentParamsMap.get("edit_card_hash"));
        payuHashes.setSaveCardHash(paymentParamsMap.get("save_card_hash"));
        payuHashes.setVasForMobileSdkHash(paymentParamsMap.get("mobile_sdk_hash"));
        //payuHashes.setMerchantIbiboCodesHash(paymentParamsMap);

        paymentParams.setHash(payuHashes.getPaymentHash());

        Intent intent = new Intent(ctx, PayUBaseActivity.class);
        PayuConfig payuConfig = new PayuConfig();
        payuConfig.setEnvironment(BuildConfig.DEBUG ? PayuConstants.MOBILE_STAGING_ENV:
            PayuConstants.PRODUCTION_ENV);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
        intent.putExtra(PayuConstants.SALT, paymentParamsMap.get(PayuConstants.SALT));

        ctx.startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }
}
