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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.payu.india.Model.Emi;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.payuui.adapter.PayUEmiDurationAdapter;
import com.payu.payuui.adapter.PayUEmiNameAdapter;

import java.util.ArrayList;


public class PayUEmiActivity extends PaymentBaseActivity implements View.OnClickListener {

    private Spinner emiDurationSpinner;
    private SpinnerAdapter emiDurationAdapter;
    private Emi selectedEmi;

    private EditText cardNumberEditText;
    private EditText nameOnCardEditText;
    private EditText cvvEditText;
    private EditText expiryMonthEditText;
    private EditText expiryYearEditText;

    private ArrayList<Emi> emiArrayList;

    private PaymentParams mPaymentParams;

    private PayuConfig payuConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emi);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.emi));

        Bundle bundle = getIntent().getExtras();

        Spinner bankNameSpinner = (Spinner) findViewById(R.id.spinner_emi_bank_name);
        emiDurationSpinner = (Spinner) findViewById(R.id.spinner_emi_duration);
        cardNumberEditText = (EditText) findViewById(R.id.edit_text_emi_card_number);
        nameOnCardEditText = (EditText) findViewById(R.id.edit_text_emi_name_on_card);
        cvvEditText = (EditText) findViewById(R.id.edit_text_emi_cvv);
        expiryMonthEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_month);
        expiryYearEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_year);


        // lets set the paymentdefault params and payu hashes;
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        PayuHashes payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        // sethash
        mPaymentParams.setHash(payuHashes.getPaymentHash());

        findViewById(R.id.button_emi_pay_now).setOnClickListener(this);

        if (bundle.getParcelableArrayList(PayuConstants.EMI) != null) {
            // okay we have emi now!
            // lets setup emi name adapter.
            emiArrayList = bundle.getParcelableArrayList(PayuConstants.EMI);
            SpinnerAdapter emiNameAdapter = new PayUEmiNameAdapter(this, emiArrayList);
            bankNameSpinner.setAdapter(emiNameAdapter);


            bankNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Item selected, lets setup the emiDuration adapter.
                    emiDurationAdapter = new PayUEmiDurationAdapter(PayUEmiActivity.this, emiArrayList, (Emi) parent.getSelectedItem());
                    emiDurationSpinner.setAdapter(emiDurationAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            emiDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedEmi = (Emi) parent.getSelectedItem();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            Toast.makeText(this, "Could not find emil list from the privious activity", Toast.LENGTH_LONG).show();
        }

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
        if (v.getId() == R.id.button_emi_pay_now) {// paynow button is clicked.

            // card details
            mPaymentParams.setNameOnCard(nameOnCardEditText.getText().toString());
            mPaymentParams.setCardNumber(cardNumberEditText.getText().toString());
            mPaymentParams.setCvv(cvvEditText.getText().toString());
            mPaymentParams.setExpiryYear(expiryYearEditText.getText().toString());
            mPaymentParams.setExpiryMonth(expiryMonthEditText.getText().toString());

            // bank code
            mPaymentParams.setBankCode(selectedEmi.getBankCode());

            PostData postData = new PaymentPostParams(mPaymentParams, PayuConstants.EMI).getPaymentPostParams();

            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // launch webview
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                /***error when the postdata for the emi entered is not correct***/
                handleUnknownErrorCondition();
            }

        }
    }
}
