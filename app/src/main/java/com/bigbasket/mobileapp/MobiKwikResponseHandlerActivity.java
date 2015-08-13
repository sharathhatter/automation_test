package com.bigbasket.mobileapp;

import android.content.Intent;

import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by jugal on 4/8/15.
 */
public class MobiKwikResponseHandlerActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobikwik_response);
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderid");
        String statusCode = intent.getStringExtra("statuscode");
        String statusMessage = intent.getStringExtra("statusmessage");
        String amount = intent.getStringExtra("amount");

        String msg = "Txn Response from Mobikwik: orderid: " + orderId
                + " , statuscode: " + statusCode + " , statusmessage: "
                + statusMessage + " , amount: " + amount;

        TextView txtResponse = (TextView) findViewById(R.id.txtMessage);
        txtResponse.setText(msg);

    }
}