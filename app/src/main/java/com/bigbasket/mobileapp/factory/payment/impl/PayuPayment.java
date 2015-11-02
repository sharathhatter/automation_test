package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.PayUBaseActivity;

import java.util.HashMap;

public class PayuPayment {

    public void startPaymentGateway(HashMap<String, String> paymentParamsMap,
                                    Activity ctx) {
        if (ctx instanceof PaymentTxnInfoAware) {
            ((PaymentTxnInfoAware) ctx).setTxnId(paymentParamsMap.get(PayuConstants.TXNID));
        }
        PaymentParams paymentParams = new PaymentParams();
        paymentParams.setKey(paymentParamsMap.get(PayuConstants.KEY));
        paymentParams.setAmount(paymentParamsMap.get(PayuConstants.AMOUNT));
        paymentParams.setProductInfo(paymentParamsMap.get(PayuConstants.PRODUCT_INFO));
        paymentParams.setFirstName(paymentParamsMap.get(PayuConstants.FIRST_NAME));
        paymentParams.setLastName(paymentParamsMap.get(PayuConstants.LASTNAME));
        paymentParams.setEmail(paymentParamsMap.get(PayuConstants.EMAIL));
        paymentParams.setTxnId(paymentParamsMap.get(PayuConstants.TXNID));
        paymentParams.setSurl(paymentParamsMap.get(PayuConstants.SURL));
        paymentParams.setFurl(paymentParamsMap.get(PayuConstants.FURL));
        paymentParams.setOfferKey(paymentParamsMap.get(PayuConstants.OFFER_KEY));

        if (paymentParamsMap.get(PayuConstants.UDF1) != null) {
            paymentParams.setUdf1(paymentParamsMap.get(PayuConstants.UDF1));
        } else {
            paymentParams.setUdf1("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF2) != null) {
            paymentParams.setUdf2(paymentParamsMap.get(PayuConstants.UDF2));
        } else {
            paymentParams.setUdf2("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF3) != null) {
            paymentParams.setUdf3(paymentParamsMap.get(PayuConstants.UDF3));
        } else {
            paymentParams.setUdf3("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF4) != null) {
            paymentParams.setUdf4(paymentParamsMap.get(PayuConstants.UDF4));
        } else {
            paymentParams.setUdf4("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF5) != null) {
            paymentParams.setUdf5(paymentParamsMap.get(PayuConstants.UDF5));
        } else {
            paymentParams.setUdf5("");
        }

        paymentParams.setUserCredentials(paymentParamsMap.get(PayuConstants.USER_CREDENTIALS));
        paymentParams.setPhone(paymentParamsMap.get(PayuConstants.PHONE));

        PayuHashes payuHashes = new PayuHashes();
        payuHashes.setPaymentHash(paymentParamsMap.get("payment_hash"));
        payuHashes.setVasForMobileSdkHash(paymentParamsMap.get("vas_hash"));
        payuHashes.setDeleteCardHash(paymentParamsMap.get("delete_card_hash"));
        payuHashes.setEditCardHash(paymentParamsMap.get("edit_card_hash"));
        payuHashes.setSaveCardHash(paymentParamsMap.get("save_card_hash"));
        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(paymentParamsMap.get("mobile_sdk_hash"));
        //payuHashes.setMerchantIbiboCodesHash(paymentParamsMap);

        paymentParams.setHash(payuHashes.getPaymentHash());

        Intent intent = new Intent(ctx, PayUBaseActivity.class);
        PayuConfig payuConfig = new PayuConfig();
        payuConfig.setEnvironment(BuildConfig.DEBUG ? PayuConstants.MOBILE_STAGING_ENV :
                PayuConstants.PRODUCTION_ENV);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);

        ctx.startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }
}
