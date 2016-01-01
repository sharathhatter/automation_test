package com.payu.payuui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.ArrayList;


public class PayUCashCardActivity extends PaymentBaseActivity implements AdapterView.OnItemClickListener {

    ListView cashCardListView;
    PayUCashCardAdapter payUCashCardAdapter;
    ArrayList<PaymentDetails> mCashCardList;
    Bundle bundle;
    //    PaymentDefaultParams mPaymentDefaultParams;
    PaymentParams mPaymentParams;
    PayuHashes mPayuHashes;
    private Toolbar toolbar;
    private TextView amountTextView;
    private TextView txnIdTextView;

    private PayuConfig payuConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_card);

        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.paymentviapayu));

        cashCardListView = (ListView) findViewById(R.id.list_view_cash_card);
        cashCardListView.setOnItemClickListener(this);
        bundle = getIntent().getExtras();


        // lets get the default params and hashes
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        mPayuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams.setHash(mPayuHashes.getPaymentHash());

        (amountTextView = (TextView) findViewById(R.id.text_view_amount)).setText(getString(R.string.amount, mPaymentParams.getAmount()));
        (txnIdTextView = (TextView) findViewById(R.id.text_view_transaction_id)).setText(getString(R.string.transaction_id, mPaymentParams.getTxnId()));

        // lets get the list of cash card from bundle.
        if (bundle.getParcelableArrayList(PayuConstants.CASHCARD) != null) {
            mCashCardList = bundle.getParcelableArrayList(PayuConstants.CASHCARD);
            payUCashCardAdapter = new PayUCashCardAdapter(this, R.layout.cash_card_list_item, mCashCardList);
            cashCardListView.setAdapter(payUCashCardAdapter);

            // lets set the mandatory params

        } else {
            handleUnknownErrorCondition();
        }


        /*******************setting status bar color**************/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.uiv3_status_bar_background));
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // lets validate all required params
//        PostData postData = new CashCardPostParams(mPaymentDefaultParams, mCashCardList.get(position)).getCashPostParams();
        mPaymentParams.setBankCode(mCashCardList.get(position).getBankCode());
        PostData postData = new PaymentPostParams(mPaymentParams, PayuConstants.CASH).getPaymentPostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // launch webview
            payuConfig.setData(postData.getResult());
            Intent intent = new Intent(this, PaymentsActivity.class);
            intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            /*****if the cash card payment params has issue****/
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

}

class PayUCashCardAdapter extends ArrayAdapter<PaymentDetails> {
    Context mContext;
    ArrayList<PaymentDetails> mCashCardList;

    public PayUCashCardAdapter(Context context, int resource, ArrayList<PaymentDetails> cashCardList) {
        super(context, resource, cashCardList);
        mContext = context;
        mCashCardList = cashCardList;
    }

    @Override
    public int getCount() {
        if (null != mCashCardList) return mCashCardList.size();
        else return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CashCardViewHolder cashCardViewHolder = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.cash_card_list_item, null);
            cashCardViewHolder = new CashCardViewHolder(convertView);
            convertView.setTag(cashCardViewHolder);
        } else {
            cashCardViewHolder = (CashCardViewHolder) convertView.getTag();
        }

        PaymentDetails paymentDetails = mCashCardList.get(position);

        // set text here
        cashCardViewHolder.cashCardTextView.setText(paymentDetails.getBankName());
        return convertView;
    }


    class CashCardViewHolder {
        TextView cashCardTextView;

        CashCardViewHolder(View view) {
            cashCardTextView = (TextView) view.findViewById(R.id.text_view_cash_card);
        }
    }
}

