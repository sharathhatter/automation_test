package com.payu.payuui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.PaymentPostParams;


public class PayUCreditDebitCardActivity extends PaymentBaseActivity implements View.OnClickListener {

    int storeOneClickHash;
    private EditText cardNameEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private CheckBox saveCardCheckBox;
    private CheckBox enableOneClickPaymentCheckBox;
    private PayuHashes mPayuHashes;
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private PayuUtils payuUtils;
    private TextInputLayout cardNumberInputLayout;
    private TextInputLayout cardNameInputLayout;
    private TextInputLayout expMonthInputLayout;
    private TextInputLayout expYearInputLayout;
    private TextInputLayout cvvInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.credit_debit_card));

        findViewById(R.id.button_card_make_payment).setOnClickListener(this);

        cardNameEditText = (EditText) findViewById(R.id.edit_text_name_on_card);
        cardNumberEditText = (EditText) findViewById(R.id.edit_text_card_number);
        cardCvvEditText = (EditText) findViewById(R.id.edit_text_card_cvv);
        cardExpiryMonthEditText = (EditText) findViewById(R.id.edit_text_expiry_month);
        cardExpiryYearEditText = (EditText) findViewById(R.id.edit_text_expiry_year);
        saveCardCheckBox = (CheckBox) findViewById(R.id.check_box_save_card);
        enableOneClickPaymentCheckBox = (CheckBox) findViewById(R.id.check_box_enable_one_click_payment);

        cardNumberInputLayout =  (TextInputLayout)findViewById(R.id.card_number_input_layout);
        cardNameInputLayout =  (TextInputLayout)findViewById(R.id.card_name_input_layout);
        expMonthInputLayout =  (TextInputLayout)findViewById(R.id.exp_month_input_layout);
        expYearInputLayout =  (TextInputLayout)findViewById(R.id.exp_year_input_layout);
        cvvInputLayout =  (TextInputLayout)findViewById(R.id.cvv_input_layout);


        Bundle bundle = getIntent().getExtras();

        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_NONE)
            enableOneClickPaymentCheckBox.setVisibility(View.GONE);

        // lets get payment default params and hashes
        mPayuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        // lets not show the save card check box if user credentials is not found!
        if (null == mPaymentParams.getUserCredentials()) {
            saveCardCheckBox.setVisibility(View.GONE);
            enableOneClickPaymentCheckBox.setVisibility(View.GONE);
        } else {
            saveCardCheckBox.setVisibility(View.VISIBLE);
        }

        payuUtils = new PayuUtils();


        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            String issuer;
            Drawable issuerDrawable;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 5) { // to confirm rupay card we need min 6 digit.
                    if (null == issuer) issuer = payuUtils.getIssuer(charSequence.toString());
                    if (issuer != null && issuer.length() > 1 && issuerDrawable == null) {
                        issuerDrawable = getIssuerDrawable(issuer);
                        if (issuer.contentEquals(PayuConstants.SMAE)) { // hide cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.GONE);
                            cardExpiryYearEditText.setVisibility(View.GONE);
                            cardCvvEditText.setVisibility(View.GONE);
                        } else { //show cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.VISIBLE);
                            cardExpiryYearEditText.setVisibility(View.VISIBLE);
                            cardCvvEditText.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    issuer = null;
                    issuerDrawable = null;
                }
                cardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

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
        // Oh crap! Resource IDs cannot be used in a switch statement in Android library modules less... (Ctrl+F1)
        // Validates using resource IDs in a switch statement in Android library module
        // we cant not use switch and gotta use simple if else
        if (v.getId() == R.id.button_card_make_payment) {

            // do i have to store the card
            if (saveCardCheckBox.isChecked()) {
                mPaymentParams.setStoreCard(1);
            } else {
                mPaymentParams.setStoreCard(0);
            }


            // do i have to store the cvv
            if (enableOneClickPaymentCheckBox.isChecked()) {
                mPaymentParams.setEnableOneClickPayment(1);
            } else {
                mPaymentParams.setEnableOneClickPayment(0);
            }

            cardNameInputLayout.setErrorEnabled(false);
            cardNameInputLayout.setError("");

            cardNumberInputLayout.setErrorEnabled(false);
            cardNumberInputLayout.setError("");

            expMonthInputLayout.setErrorEnabled(false);
            expMonthInputLayout.setError("");

            expYearInputLayout.setErrorEnabled(false);
            expYearInputLayout.setError("");

            cvvInputLayout.setErrorEnabled(false);
            cvvInputLayout.setError("");

            // setup the hash
            mPaymentParams.setHash(mPayuHashes.getPaymentHash());

            // lets get the current card number;
            String cardNumber = String.valueOf(cardNumberEditText.getText());
            String cardName = cardNameEditText.getText().toString();
            String expiryMonth = cardExpiryMonthEditText.getText().toString();
            String expiryYear = cardExpiryYearEditText.getText().toString();
            String cvv = cardCvvEditText.getText().toString();

            // lets not worry about ui validations.
            mPaymentParams.setCardNumber(cardNumber);
            mPaymentParams.setCardName(cardName);
            mPaymentParams.setNameOnCard(cardName);
            mPaymentParams.setExpiryMonth(expiryMonth);
            mPaymentParams.setExpiryYear(expiryYear);
            mPaymentParams.setCvv(cvv);

            // lets try to get the post params
            PostData postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // okay good to go.. lets make a transaction
                // launch webview
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);

            } else {
                TextInputLayout inputLayout = null;
                EditText editText = null;
                switch (postData.getCode()) {
                    case PayuErrors.INVALID_CARD_NUMBER_EXCEPTION:
                        inputLayout = cardNumberInputLayout;
                        editText = cardNumberEditText;
                        break;
                    case PayuErrors.INVALID_MONTH_EXCEPTION:
                        inputLayout = expMonthInputLayout;
                        editText = cardExpiryMonthEditText;
                        break;
                    case PayuErrors.INVALID_YEAR_EXCEPTION:
                        inputLayout = expYearInputLayout;
                        editText = cardExpiryYearEditText;
                        break;
                    case PayuErrors.INVALID_CVV_EXCEPTION:
                        inputLayout = cvvInputLayout;
                        editText = cardCvvEditText;
                        break;

                }
                if(inputLayout != null && editText != null) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError(postData.getResult());
                    editText.requestFocus();
                } else {
                    /***error when the postdata for the card entered is not correct***/
                    handleUnknownErrorCondition(postData.getResult(), false);
                }
            }
        } else {
            /***error if the click from something else****/
            handleUnknownErrorCondition();
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


    private Drawable getIssuerDrawable(String issuer) {

        switch (issuer) {
            case PayuConstants.VISA:
                return ContextCompat.getDrawable(this, R.drawable.visa);
            case PayuConstants.LASER:
                return ContextCompat.getDrawable(this, R.drawable.laser);
            case PayuConstants.DISCOVER:
                return ContextCompat.getDrawable(this, R.drawable.discover);
            case PayuConstants.MAES:
                return ContextCompat.getDrawable(this, R.drawable.maestro);
            case PayuConstants.MAST:
                return ContextCompat.getDrawable(this, R.drawable.master);
            case PayuConstants.AMEX:
                return ContextCompat.getDrawable(this, R.drawable.amex);
            case PayuConstants.DINR:
                return ContextCompat.getDrawable(this, R.drawable.diner);
            case PayuConstants.JCB:
                return ContextCompat.getDrawable(this, R.drawable.jcb);
            case PayuConstants.SMAE:
                return ContextCompat.getDrawable(this, R.drawable.maestro);
            case PayuConstants.RUPAY:
                return ContextCompat.getDrawable(this, R.drawable.rupay);
        }
        return null;

    }
}
