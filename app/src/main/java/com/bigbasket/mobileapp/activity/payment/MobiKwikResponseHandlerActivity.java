package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.util.Constants;

/**
 * Created by jugal on 4/8/15.
 */
public class MobiKwikResponseHandlerActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobikwik_response);
        Intent intent = getIntent();
        String txnId = intent.getStringExtra(Constants.MOBIKWIK_ORDER_ID);
        String statusCode = intent.getStringExtra(Constants.MOBIKWIK_STATUS_CODE);
        String statusMessage = intent.getStringExtra(Constants.MOBIKWIK_RESPONSE_STATUS_MSG);
        //String amount = intent.getStringExtra("amount");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.MOBIKWIK_ORDER_ID, txnId);
        editor.putString(Constants.MOBIKWIK_STATUS, statusCode);
        editor.putString(Constants.MOBIKWIK_STATUS_MSG, statusMessage);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        onDestroy();
    }
}