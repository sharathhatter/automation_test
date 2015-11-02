package com.bigbasket.mobileapp.activity.payment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.util.Constants;

public class MobiKwikResponseHandlerActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobikwik_response);
        Intent intent = getIntent();
        String txnId = intent.getStringExtra(Constants.MOBIKWIK_ORDER_ID);
        String statusCode = intent.getStringExtra(Constants.MOBIKWIK_STATUS_CODE);

        SharedPreferences.Editor editor = getSharedPreferences(Constants.MOBIKWIK_PREFERENCE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(Constants.MOBIKWIK_ORDER_ID, txnId)
                .putString(Constants.MOBIKWIK_STATUS, statusCode)
                .apply();
        finish();
    }
}