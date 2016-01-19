package com.bigbasket.mobileapp.factory.payment.impl;

import android.app.Activity;
import android.content.Intent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.Transaction;
import com.mobikwik.sdk.lib.TransactionConfiguration;
import com.mobikwik.sdk.lib.User;
import com.mobikwik.sdk.lib.payinstrument.PaymentInstrumentType;

import java.util.HashMap;

public class MobikwikPayment {

    public static final int MOBIKWIK_REQ_CODE = 1000;

    public static final String MOBIKWIK_CELL = "cell";
    public static final String MOBIKWIK_EMAIL = "email";
    public static final String MOBIKWIK_AMOUNT = "amount";
    public static final String MOBIKWIK_ORDERID = "orderid";
    public static final String MOBIKWIK_PGRESPONSEURL = "pgResponseUrl";
    public static final String MOBIKWIK_MID = "mid";
    public static final String MOBIKWIK_CHECKSUMURL = "checksumUrl";
    public static final String MOBIKWIK_DEBITWALLET = "debitWallet";
    public static final String MOBIKWIK_MERCHANTNAME = "merchantname";

    public static void startPaymentGateway(HashMap<String, String> paymentParams, Activity ctx) {

        TransactionConfiguration config = new TransactionConfiguration();
        config.setDebitWallet(Boolean.valueOf(paymentParams.get(MOBIKWIK_DEBITWALLET)));
        config.setPgResponseUrl(paymentParams.get(MOBIKWIK_PGRESPONSEURL));
        config.setChecksumUrl(paymentParams.get(MOBIKWIK_CHECKSUMURL));
        config.setMerchantName(paymentParams.get(MOBIKWIK_MERCHANTNAME));
        config.setMbkId(paymentParams.get(MOBIKWIK_MID));
        config.setMode(((AppOperationAware) ctx).getCurrentActivity().getResources().getString(R.string.mobikwik_mode));

        User user = new User(paymentParams.get(MOBIKWIK_EMAIL), paymentParams.get(MOBIKWIK_CELL));
        Transaction transaction = Transaction.Factory.newTransaction(user, paymentParams.get(MOBIKWIK_ORDERID), paymentParams.get(MOBIKWIK_AMOUNT), PaymentInstrumentType.MK_WALLET);

        Intent mobikwikIntent = new Intent(ctx, MobikwikSDK.class);
        mobikwikIntent.setPackage(ctx.getPackageName());
        mobikwikIntent.setType("text/plain");
        mobikwikIntent.putExtra(MobikwikSDK.EXTRA_TRANSACTION_CONFIG, config);
        mobikwikIntent.putExtra(MobikwikSDK.EXTRA_TRANSACTION, transaction);
        ctx.startActivityForResult(mobikwikIntent, MOBIKWIK_REQ_CODE);
    }
}
