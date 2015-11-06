package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.model.holders.InMemMobikwikResponseHolder;

public class MobiKwikResponseHandlerActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobikwik_response);
        Intent intent = getIntent();
        String txnId = intent.getStringExtra("orderid");
        String statusCode = intent.getStringExtra("statuscode");

        ((BaseApplication) getApplication())
                .setInMemMobikwikResponseHolder(new InMemMobikwikResponseHolder(txnId, statusCode));
        finish();
    }
}