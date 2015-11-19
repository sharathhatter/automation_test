package com.bigbasket.mobileapp.activity.payment;

import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.handler.payment.MobikwikResponseHandler;

public class MobiKwikResponseHandlerActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobikwik_response);
        MobikwikResponseHandler.setMobikwikTransaction(getIntent());
        finish();
    }
}