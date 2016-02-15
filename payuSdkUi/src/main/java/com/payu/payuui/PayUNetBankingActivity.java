package com.payu.payuui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.payuui.adapter.PayUNetBankingAdapter;

import java.util.ArrayList;


public class PayUNetBankingActivity extends PaymentBaseActivity implements View.OnClickListener {

    private String bankcode;
    private ArrayList<PaymentDetails> netBankingList;
    //    private String[] netBanksNamesArray;
//    private String[] netBanksCodesArray;
    private PaymentParams mPaymentParams;
    private PayuHashes payuHashes;

    private PayuConfig payuConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_banking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.net_banking));

        findViewById(R.id.button_pay_now).setOnClickListener(this);
        Spinner spinnerNetbanking = (Spinner) findViewById(R.id.spinner_netbanking);


        // lets get the required data form bundle
        Bundle bundle = getIntent().getExtras();

        if (bundle != null && bundle.getParcelableArrayList(PayuConstants.NETBANKING) != null) {
            netBankingList = new ArrayList<PaymentDetails>();
            netBankingList = bundle.getParcelableArrayList(PayuConstants.NETBANKING);

//            // initialize
//            netBanksNamesArray = new String[netBankingList.size()];
//            netBanksCodesArray = new String[netBankingList.size()];
//
//            for (int i = 0; i < netBankingList.size(); i++) {
//                netBanksNamesArray[i] = netBankingList.get(i).getBankName();
//                netBanksCodesArray[i] = netBankingList.get(i).getBankCode();
//            }

            PayUNetBankingAdapter payUNetBankingAdapter = new PayUNetBankingAdapter(this, netBankingList);
            spinnerNetbanking.setAdapter(payUNetBankingAdapter);
            spinnerNetbanking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    bankcode = netBankingList.get(index).getBankCode();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            Toast.makeText(this, R.string.empty_netbanking_list_error, Toast.LENGTH_LONG).show();
            setResult(Constants.RESULT_REFRESH_DETAILS);
            finish();
            return;
        }

        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        /*******************setting status bar color**************/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.uiv3_status_bar_background));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_pay_now) {
            // okey we need hash fist
            PostData postData = new PostData();
            mPaymentParams.setHash(payuHashes.getPaymentHash());
            mPaymentParams.setBankCode(bankcode);

            postData = new PaymentPostParams(mPaymentParams, PayuConstants.NB).getPaymentPostParams();

//            postData = new NBPostParams(mPaymentParams, mNetBank).getNBPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // launch webview
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                /***error when the postdata for the netbanking entered is not correct***/
                handleUnknownErrorCondition();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
        } else {
            if (data != null) {
                data.putExtra("transaction_status", false);
            }
            setResult(resultCode, data);
            finish();
        }
    }
}
