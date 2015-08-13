package com.bigbasket.mobileapp;


import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.handler.payment.PaymentInitiator;
import com.bigbasket.mobileapp.interfaces.payment.PayuPaymentAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.apache.http.protocol.HTTP;

import java.util.HashMap;

/**
 * Created by jugal on 4/8/15.
 */
public class MobiKwikActivity extends BackButtonActivity implements PayuPaymentAware {

    private String mPotentialOrderId, payment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobiwik_start_btn);
        mPotentialOrderId = getIntent().getStringExtra("pid");
        payment = getIntent().getStringExtra("payment");
    }

    public void onStartMobiKwikClick(View view) {
        new PaymentInitiator<>(this, mPotentialOrderId, Constants.MOBIKWIK_PAYMENT)
                .initiate();
    }


    public void callMobiKWikSDK(HashMap<String, String> paymentParams){
        String amount = UIUtil.round(Double.parseDouble(paymentParams.get(Constants.MOBIKWIK_AMOUNT)));
        Intent walletIntent = new Intent(Constants.MOBIKWIKSDK);
        walletIntent.setPackage(getPackageName());
        walletIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        walletIntent.putExtra(Constants.MOBIKWIK_ORDER_ID, paymentParams.get(Constants.MOBIKWIK_ORDER_ID));
        walletIntent.putExtra(Constants.MOBIKWIK_DEBIT_WALLET, paymentParams.get(Constants.MOBIKWIK_DEBIT_WALLET));
        walletIntent.putExtra(Constants.MOBIKWIK_AMOUNT, amount);
        walletIntent.putExtra(Constants.MOBIKWIK_EMAIL, paymentParams.get(Constants.MOBIKWIK_EMAIL));
        walletIntent.putExtra(Constants.MOBIKWIK_CELL, paymentParams.get(Constants.MOBIKWIK_CELL));
        walletIntent.putExtra(Constants.MOBIKWIK_MID, paymentParams.get(Constants.MOBIKWIK_MID));
        walletIntent.putExtra(Constants.MOBIKWIK_MERCHANT_NAME, paymentParams.get(Constants.MOBIKWIK_MERCHANT_NAME));
        walletIntent.putExtra(Constants.MOBIKWIK_MODE, Constants.MOBIKWIK_MODE_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_SDK_SIGN, Constants.MOBIKWIK_SDK_SIGN_VALUE);
        walletIntent.putExtra(Constants.MOBIKWIK_REDIRECT_ANDROID_CLASS_NAME,
                Constants.MOBIKWIK_RESPONSE_HANDLER_CLASS);
        walletIntent.putExtra(Constants.MOBIKWIK_CHECKSUM_URL,
                paymentParams.get(Constants.MOBIKWIK_CHECKSUM_URL));
        walletIntent.putExtra(Constants.MOBIKWIK_CALL_BACK_URL,
                paymentParams.get(Constants.MOBIKWIK_CALL_BACK_URL));
        startActivity(walletIntent);
    }

    @Override
    public void initializePayu(HashMap<String, String> paymentParams) {
        callMobiKWikSDK(paymentParams);
    }

    /**
     *
     *  sdk sign, test_account for testing
     *
     */
}