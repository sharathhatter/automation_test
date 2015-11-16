package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.crashlytics.android.Crashlytics;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.PayUBaseActivity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PayuPayment {

    public static void startPaymentGateway(HashMap<String, String> paymentParamsInput,
                                    Activity ctx) {

        // Copying into another hash-map
        HashMap<String, String> paymentParamsMap = new HashMap<>(paymentParamsInput);
        if (ctx instanceof PaymentTxnInfoAware) {
            ((PaymentTxnInfoAware) ctx).setTxnId(paymentParamsMap.get(PayuConstants.TXNID));
        }

        PaymentParams paymentParams = new PaymentParams();
        paymentParams.setKey(paymentParamsMap.remove(PayuConstants.KEY));
        paymentParams.setAmount(paymentParamsMap.remove(PayuConstants.AMOUNT));
        paymentParams.setProductInfo(paymentParamsMap.remove(PayuConstants.PRODUCT_INFO));
        paymentParams.setFirstName(paymentParamsMap.remove(PayuConstants.FIRST_NAME));
        paymentParams.setLastName(paymentParamsMap.remove(PayuConstants.LASTNAME));
        paymentParams.setEmail(paymentParamsMap.remove(PayuConstants.EMAIL));
        paymentParams.setTxnId(paymentParamsMap.remove(PayuConstants.TXNID));
        paymentParams.setSurl(paymentParamsMap.remove(PayuConstants.SURL));
        paymentParams.setFurl(paymentParamsMap.remove(PayuConstants.FURL));
        paymentParams.setOfferKey(paymentParamsMap.remove(PayuConstants.OFFER_KEY));

        if (paymentParamsMap.get(PayuConstants.UDF1) != null) {
            paymentParams.setUdf1(paymentParamsMap.remove(PayuConstants.UDF1));
        } else {
            paymentParams.setUdf1("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF2) != null) {
            paymentParams.setUdf2(paymentParamsMap.remove(PayuConstants.UDF2));
        } else {
            paymentParams.setUdf2("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF3) != null) {
            paymentParams.setUdf3(paymentParamsMap.remove(PayuConstants.UDF3));
        } else {
            paymentParams.setUdf3("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF4) != null) {
            paymentParams.setUdf4(paymentParamsMap.remove(PayuConstants.UDF4));
        } else {
            paymentParams.setUdf4("");
        }
        if (paymentParamsMap.get(PayuConstants.UDF5) != null) {
            paymentParams.setUdf5(paymentParamsMap.remove(PayuConstants.UDF5));
        } else {
            paymentParams.setUdf5("");
        }

        paymentParams.setUserCredentials(paymentParamsMap.remove(PayuConstants.USER_CREDENTIALS));
        paymentParams.setPhone(paymentParamsMap.remove(PayuConstants.PHONE));

        PayuHashes payuHashes = new PayuHashes();
        payuHashes.setPaymentHash(paymentParamsMap.remove("payment_hash"));
        payuHashes.setVasForMobileSdkHash(paymentParamsMap.remove("vas_hash"));
        payuHashes.setDeleteCardHash(paymentParamsMap.remove("delete_card_hash"));
        payuHashes.setEditCardHash(paymentParamsMap.remove("edit_card_hash"));
        payuHashes.setSaveCardHash(paymentParamsMap.remove("save_card_hash"));
        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(paymentParamsMap.remove("mobile_sdk_hash"));
        paymentParams.setHash(payuHashes.getPaymentHash());

        // Setup any remaining param that server is passing using reflection
        Class<?> pClass = paymentParams.getClass();
        for (Map.Entry<String, String> entry : paymentParamsMap.entrySet()) {
            try {
                Field field = pClass.getDeclaredField(getUnderscoreFieldNameAsCamelCase(entry.getKey()));
                field.setAccessible(true);
                field.set(paymentParams, entry.getValue());
            } catch (NoSuchFieldException e) {
                // Do nothing
            } catch (IllegalAccessException e) {
                Crashlytics.logException(e);
            }
        }

        Intent intent = new Intent(ctx, PayUBaseActivity.class);
        PayuConfig payuConfig = new PayuConfig();
        payuConfig.setEnvironment(BuildConfig.DEBUG ? PayuConstants.MOBILE_STAGING_ENV :
                PayuConstants.PRODUCTION_ENV);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);

        ctx.startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    private static String getUnderscoreFieldNameAsCamelCase(String fieldName) {
        String[] splits = fieldName.split("_");
        if (splits.length == 1) return splits[0];
        StringBuilder builder = new StringBuilder(splits[0]);
        for (int i = 1; i < splits.length; i++) {
            String splittedStr = splits[i];
            builder.append(Character.toUpperCase(splittedStr.charAt(0)));
            builder.append(splittedStr.substring(1, splittedStr.length()));
        }
        return builder.toString();
    }
}
