package com.payu.payuui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import java.util.ArrayList;
import java.util.HashMap;


public class PayUBaseActivity extends PaymentBaseActivity implements View.OnClickListener, PaymentRelatedDetailsListener {

    PayuResponse mPayuResponse;
    Intent mIntent;
    Button netBankingButton;
    Button emiButton;
    Button cashCardButton;
    Button payUMoneyButton;
    Button storedCardButton;
    Button creditDebitButton;
    Button merchantPaymentButton;
    Button oneClickPaymentButton;
    PayuConfig payuConfig;

    ArrayList<StoredCard> storedCards;
    ArrayList<StoredCard> oneClickCards;
    HashMap<String, String> oneClickCardTokens;


    //    PaymentDefaultParams mPaymentDefaultParams;

    PaymentParams mPaymentParams;
    PayuHashes mPayUHashes;

    int storeOneClickHash;
    Bundle bundle;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.paymentviapayu));

        storedCards = new ArrayList<>();
        oneClickCards = new ArrayList<>();

        // leets register the buttons
        (netBankingButton = (Button) findViewById(R.id.button_netbanking)).setOnClickListener(this);
        (emiButton = (Button) findViewById(R.id.button_emi)).setOnClickListener(this);
        (cashCardButton = (Button) findViewById(R.id.button_cash_card)).setOnClickListener(this);
        (payUMoneyButton = (Button) findViewById(R.id.button_payumoney)).setOnClickListener(this);
        (storedCardButton = (Button) findViewById(R.id.button_stored_card)).setOnClickListener(this);
        (creditDebitButton = (Button) findViewById(R.id.button_credit_debit_card)).setOnClickListener(this);
        (merchantPaymentButton = (Button) findViewById(R.id.button_merchant_payment)).setOnClickListener(this);
        (oneClickPaymentButton = (Button) findViewById(R.id.button_one_click_payment)).setOnClickListener(this);

        // lets collect the details from bundle to fetch the payment related details for a merchant
        bundle = getIntent().getExtras();

        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        // TODO add null pointer check here
//        mPaymentDefaultParams = bundle.getParcelable(PayuConstants.PAYMENT_DEFAULT_PARAMS);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        mPayUHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);

        ((TextView) findViewById(R.id.text_view_amount)).setText(getString(R.string.amount, mPaymentParams.getAmount()));
        ((TextView) findViewById(R.id.text_view_transaction_id)).setText(getString(R.string.transaction_id, mPaymentParams.getTxnId()));


        oneClickCardTokens = (HashMap<String, String>) bundle.getSerializable(PayuConstants.ONE_CLICK_CARD_TOKENS);


        /*******************setting status bar color**************/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.uiv3_status_bar_background));
        }


        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials() == null ? "default" : mPaymentParams.getUserCredentials());

        // hash we have to generate


        merchantWebService.setHash(mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash());

//        PostData postData = new PostParams(merchantWebService).getPostParams();

        // Dont fetch the data if calling activity is PaymentActivity

        // fetching for the first time.
        if (null == savedInstanceState) { // dont fetch the data if its been called from payment activity.
            PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // ok we got the post params, let make an api call to payu to fetch the payment related details
                payuConfig.setData(postData.getResult());

                // lets set the visibility of progress bar
                findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

                //Checking if the PayU is selected by the user
                boolean isPayUSelected = getIntent().getBooleanExtra(Constants.PAYU_SELECTED, false);
                if (isPayUSelected) {
                    launchPayumoney();
                } else {
                    fetchPaymentRelatedDetails();
                }
            } else {
                /****error in getting merchant post params***/
                // close the progress bar
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                handleUnknownErrorCondition();
            }
        }
    }


    private void fetchPaymentRelatedDetails() {
        GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(this);
        paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
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
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (data.hasExtra("transaction_status")) {
                setResult(resultCode, data);
                finish();
            } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE && resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //// TODO: 29/6/15 try to use switch case coz switch case does not work well on library projects...!!!!.

        if (id == R.id.button_netbanking) {
            mIntent = new Intent(this, PayUNetBankingActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.NETBANKING, mPayuResponse.getNetBanks());
            launchActivity(mIntent);
        } else if (id == R.id.button_cash_card) {
            mIntent = new Intent(this, PayUCashCardActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.CASHCARD, mPayuResponse.getCashCard());
            launchActivity(mIntent);
        } else if (id == R.id.button_emi) {
            mIntent = new Intent(this, PayUEmiActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.EMI, mPayuResponse.getEmi());
            launchActivity(mIntent);
        } else if (id == R.id.button_credit_debit_card) {
            mIntent = new Intent(this, PayUCreditDebitCardActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.CREDITCARD, mPayuResponse.getCreditCard());
            mIntent.putParcelableArrayListExtra(PayuConstants.DEBITCARD, mPayuResponse.getDebitCard());
            launchActivity(mIntent);
        } else if (id == R.id.button_payumoney) {
            launchPayumoney();
        } else if (id == R.id.button_stored_card) {
            mIntent = new Intent(this, PayUStoredCardsActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.STORED_CARD, storedCards);
            launchActivity(mIntent);
        } else if (id == R.id.button_one_click_payment) {
            mIntent = new Intent(this, PayUOneClickPaymentActivity.class);
            mIntent.putParcelableArrayListExtra(PayuConstants.STORED_CARD, oneClickCards);
            mIntent.putExtra(PayuConstants.ONE_CLICK_CARD_TOKENS, oneClickCardTokens);
            launchActivity(mIntent);
        }
    }

    private void launchPayumoney() {
        PostData postData;

        // lets try to get the post params
        mPaymentParams.setHash(mPayUHashes.getPaymentHash());

//        postData = new PayuWalletPostParams(mPaymentDefaultParams).getPayuWalletPostParams();
        postData = new PaymentPostParams(mPaymentParams, PayuConstants.PAYU_MONEY).getPaymentPostParams();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // launch webview
            payuConfig.setData(postData.getResult());
            Intent intent = new Intent(this, PaymentsActivity.class);
            intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            /*** error if the post data is not proper transaction wont go through***/
            handleUnknownErrorCondition();

        }
    }

    private void launchActivity(Intent intent) {
        intent.putExtra(PayuConstants.PAYU_HASHES, mPayUHashes);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
        mPaymentParams.getAmount();

        payuConfig.setData(null);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);

        // salt
        if (bundle.getString(PayuConstants.SALT) != null)
            intent.putExtra(PayuConstants.SALT, bundle.getString(PayuConstants.SALT));

        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        HashMap<String, ArrayList<StoredCard>> storedCardMap = new HashMap<>();

        switch (storeOneClickHash) {
            case PayuConstants.STORE_ONE_CLICK_HASH_MOBILE:
                storedCardMap = new PayuUtils().getStoredCard(this, mPayuResponse.getStoredCards());
                storedCards = storedCardMap.get(PayuConstants.STORED_CARD);
                oneClickCards = storedCardMap.get(PayuConstants.ONE_CLICK_CHECKOUT);
                break;
            case PayuConstants.STORE_ONE_CLICK_HASH_SERVER:
                storedCardMap = new PayuUtils().getStoredCard(mPayuResponse.getStoredCards(), oneClickCardTokens);
                storedCards = storedCardMap.get(PayuConstants.STORED_CARD);
                oneClickCards = storedCardMap.get(PayuConstants.ONE_CLICK_CHECKOUT);
                break;
            case PayuConstants.STORE_ONE_CLICK_HASH_NONE: // all are stored cards.
            default:
                storeOneClickHash = 0;
                storedCards = payuResponse.getStoredCards();
                break;
        }

//        HashMap<String, ArrayList<StoredCard>> storedCardMap = new PayuUtils().getStoredCard(this, mPayuResponse.getStoredCards());
//        HashMap<String, ArrayList<StoredCard>> storedCardMap = new PayuUtils().getStoredCard(mPayuResponse.getStoredCards(), oneClickCardTokens);


        if (payuResponse.isResponseAvailable() && payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) { // ok we are good to go

            //making the view visible if payuresponse is success
            //findViewById(R.id.mOptionSelectionTextView).setVisibility(View.VISIBLE);

            if (payuResponse.isStoredCardsAvailable() && null != storedCards && storedCards.size() > 0) {
                findViewById(R.id.linear_layout_stored_card).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isStoredCardsAvailable() && oneClickCards.size() > 0) {
                findViewById(R.id.linear_layout_one_click_payment).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isNetBanksAvailable()) { // okay we have net banks now.
                findViewById(R.id.linear_layout_netbanking).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isCashCardAvailable()) { // we have cash card too
                findViewById(R.id.linear_layout_cash_card).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isCreditCardAvailable() || payuResponse.isDebitCardAvailable()) {
                findViewById(R.id.linear_layout_credit_debit_card).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isEmiAvailable()) {
                findViewById(R.id.linear_layout_emi).setVisibility(View.VISIBLE);
            }
            if (payuResponse.isPaisaWalletAvailable() && payuResponse.getPaisaWallet().get(0).getBankCode().contains(PayuConstants.PAYUW)) {
                boolean isPayUOptionVisible = getIntent().getBooleanExtra(Constants.SHOW_PAYU, false);
                if (isPayUOptionVisible)
                    findViewById(R.id.linear_layout_payumoney).setVisibility(View.GONE);
                else
                    findViewById(R.id.linear_layout_payumoney).setVisibility(View.VISIBLE);
            }
        } else {
            /****error either the payu response is not available or the response code is not success***/

            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                TransactionDialogFragment transactionDialogFragment =
                        TransactionDialogFragment.newInstance(getString(R.string.retry_message),
                                Constants.TRANSACTION_RETRY_CODE, getString(R.string.cancel),
                                getString(R.string.retry));
                transactionDialogFragment.show(fragmentManager, getClass().getName());
            } catch (Exception e) {
                Log.d(getClass().getName(), "fragment failed");
            }
        }
    }

    @Override
    public void onDialogConfirmed(int reqCode, boolean isPositive) {

        switch (reqCode) {
            case Constants.BACKPRESSED_ERROR_CODE:
                if (isPositive) {
                    setTransactionIntentResult();
                }
                break;
            case Constants.TRANSACTION_RETRY_CODE:
                if (!isPositive) {
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    fetchPaymentRelatedDetails();
                } else {
                    setTransactionIntentResult();
                }
                break;
            default:
                super.onDialogConfirmed(reqCode, isPositive);
        }

    }

    @Override
    public void onBackPressed() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            TransactionDialogFragment transactionDialogFragment = TransactionDialogFragment.newInstance(getString(R.string.cancel_message), Constants.BACKPRESSED_ERROR_CODE, getString(R.string.ok), getString(R.string.cancel));
            transactionDialogFragment.show(fragmentManager, getClass().getName());
        } catch (Exception e) {
            Log.d(getClass().getName(), "fragment failed");
        }
    }

    private void setTransactionIntentResult() {
        Intent intent = new Intent();
        intent.putExtra("result", getString(R.string.transaction_cancelled_due_back_pressed));
        intent.putExtra("transaction_status", false);
        setResult(RESULT_CANCELED, intent);
        finish();

    }

    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) return;
        IBinder token = view.getWindowToken();
        if (token == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }
}
