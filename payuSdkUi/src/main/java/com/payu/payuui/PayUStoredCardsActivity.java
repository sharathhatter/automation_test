package com.payu.payuui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Interfaces.GetStoredCardApiListener;
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
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.GetStoredCardTask;

import java.util.ArrayList;


public class PayUStoredCardsActivity extends PaymentBaseActivity implements DeleteCardApiListener, GetStoredCardApiListener {

    private ListView storedCardListView;
    private PayUStoredCardsAdapter payUStoredCardsAdapter;
    private ArrayList<StoredCard> storedCardList;

    private PayuHashes payuHashes;
    private PaymentParams mPaymentParams;

    private PayuConfig payuConfig;
    private PayuUtils payuUtils;
    private int storeOneClickHash;
    private boolean cardsDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_cards);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.stored_card));

        storedCardListView = (ListView) findViewById(R.id.list_view_user_card);

        // lets get the required data form bundle
        Bundle bundle = getIntent().getExtras();

        payuUtils = new PayuUtils();

        if (bundle != null && bundle.getParcelableArrayList(PayuConstants.STORED_CARD) != null) {
            storedCardList = new ArrayList<StoredCard>();
            storedCardList = bundle.getParcelableArrayList(PayuConstants.STORED_CARD);
            payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList);
            storedCardListView.setAdapter(payUStoredCardsAdapter);

        } else {
            // we gotta fetch data from server
            Toast.makeText(this, R.string.error_no_cards, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
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
    public void onDeleteCardApiResponse(PayuResponse payuResponse) {
        if (payuResponse.isResponseAvailable()) {
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            // there is no error, lets fetch te cards list.

            MerchantWebService merchantWebService = new MerchantWebService();
            merchantWebService.setKey(mPaymentParams.getKey());
            merchantWebService.setCommand(PayuConstants.GET_USER_CARDS);
            merchantWebService.setVar1(mPaymentParams.getUserCredentials());
            merchantWebService.setHash(payuHashes.getStoredCardsHash());

            PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
            cardsDeleted = true;
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // ok we got the post params, let make an api call to payu to fetch the payment related details

                payuConfig.setData(postData.getResult());
                payuConfig.setEnvironment(payuConfig.getEnvironment());

                GetStoredCardTask getStoredCardTask = new GetStoredCardTask(this);
                getStoredCardTask.execute(payuConfig);
            } else {
                /***error when the postdata for the card entered is not correct***/
//                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
                Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
                setResult(Constants.RESULT_REFRESH_DETAILS);
                finish();
            }
        } else {
            /***error in deleting the card***/
            handleUnknownErrorCondition();

        }
    }

    @Override
    public void onGetStoredCardApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        payUStoredCardsAdapter = null;
//        payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList=payuResponse.getStoredCards());
        // Dont display  cvvless cards.
        storedCardList = null;
        storedCardList = new PayuUtils().getStoredCard(this, payuResponse.getStoredCards()).get(PayuConstants.STORED_CARD);
        payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList);
        storedCardListView.setAdapter(payUStoredCardsAdapter);
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
    public void onBackPressed() {
        if(cardsDeleted){
            setResult(Constants.RESULT_REFRESH_DETAILS);
        }
        super.onBackPressed();
    }

    private void makePayment(StoredCard storedCard, String cvv) {
        PostData postData = new PostData();
        // lets try to get the post params
        postData = null;
        storedCard.setCvv(cvv); // make sure that you set the cvv also
        mPaymentParams.setHash(payuHashes.getPaymentHash()); // make sure that you set payment hash
        mPaymentParams.setCardToken(storedCard.getCardToken());
        mPaymentParams.setCvv(cvv);
        mPaymentParams.setNameOnCard(storedCard.getNameOnCard());
        mPaymentParams.setCardName(storedCard.getCardName());
        mPaymentParams.setExpiryMonth(storedCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(storedCard.getExpiryYear());

        postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            Intent intent = new Intent(this, PaymentsActivity.class);
            intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
//            Toast.makeText(this, postData.getResult(), Toast.LENGTH_SHORT).show();
            handleUnknownErrorCondition();
        }

    }

    private void makePayment(StoredCard storedCard, String cvv, Boolean oneClickPaymentEnabled) {
        PostData postData = new PostData();
        // lets try to get the post params
        postData = null;
        storedCard.setCvv(cvv); // make sure that you set the cvv also
        mPaymentParams.setHash(payuHashes.getPaymentHash()); // make sure that you set payment hash
        mPaymentParams.setCardToken(storedCard.getCardToken());

        mPaymentParams.setNameOnCard(storedCard.getNameOnCard());
        mPaymentParams.setCardName(storedCard.getCardName());
        mPaymentParams.setExpiryMonth(storedCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(storedCard.getExpiryYear());


//        String merchantHash;
//        if(storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER)
//            merchantHash = oneClickCardTokens.get(storedCard.getCardToken());
//        else
//            merchantHash = payuUtils.getFromSharedPreferences(PayUOneClickPaymentActivity.this, storedCard.getCardToken());
//        String merchantHash = payuUtils.getFromSharedPreferences(PayUOneClickPaymentActivity.this, storedCard.getCardToken());
//
//        if(null != merchantHash)
//            mPaymentParams.setCardCvvMerchant(merchantHash);
//
//
//
//        String merchantHash = payuUtils.getFromSharedPreferences(PayUStoredCardsActivity.this, storedCard.getCardToken());
//
//        if(storedCard.getEnableOneClickPayment() == 1 && !merchantHash.contentEquals(PayuConstants.DEFAULT)){
//            mPaymentParams.setCardCvvMerchant(merchantHash);
//        }else{
//
//        }


        mPaymentParams.setCvv(cvv);

        if (oneClickPaymentEnabled)
            mPaymentParams.setEnableOneClickPayment(1);

        postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            Intent intent = new Intent(this, PaymentsActivity.class);
            intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
            intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            /***error when the postdata for the card entered is not correct***/
            handleUnknownErrorCondition(postData.getResult(), false);
        }

    }

    private void deleteCard(StoredCard storedCard) {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setVar2(storedCard.getCardToken());
        merchantWebService.setHash(payuHashes.getDeleteCardHash());

        PostData postData = null;
        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // ok we got the post params, let make an api call to payu to fetch
            // the payment related details
            payuConfig.setData(postData.getResult());
            payuConfig.setEnvironment(payuConfig.getEnvironment());

            DeleteCardTask deleteCardTask = new DeleteCardTask(this);
            deleteCardTask.execute(payuConfig);
        } else {
            /***error when the postdata for the card entered is not correct***/
            handleUnknownErrorCondition();
        }
    }

    //Adaptor
    public class PayUStoredCardsAdapter extends BaseAdapter { // todo rename to storedcardAdapter

        private ArrayList<StoredCard> mStoredCards;
        private Context mContext;

        public PayUStoredCardsAdapter(Context context, ArrayList<StoredCard> StoredCards) {
            mContext = context;
            mStoredCards = StoredCards;
        }

        private void viewHolder(ViewHolder holder, int position) {
//            holder.setPosition(position);
            String issuer = payuUtils.getIssuer(mStoredCards.get(position).getCardBin());
            switch (issuer) {
                case PayuConstants.VISA:
                    holder.cardIconImageView.setImageResource(R.drawable.visa);
                    break;
                case PayuConstants.LASER:
                    holder.cardIconImageView.setImageResource(R.drawable.laser);
                    break;
                case PayuConstants.DISCOVER:
                    holder.cardIconImageView.setImageResource(R.drawable.discover);
                    break;
                case PayuConstants.MAES:
                    holder.cardIconImageView.setImageResource(R.drawable.maestro);
                    break;
                case PayuConstants.MAST:
                    holder.cardIconImageView.setImageResource(R.drawable.master);
                    break;
                case PayuConstants.AMEX:
                    holder.cardIconImageView.setImageResource(R.drawable.amex);
                    break;
                case PayuConstants.DINR:
                    holder.cardIconImageView.setImageResource(R.drawable.diner);
                    break;
                case PayuConstants.JCB:
                    holder.cardIconImageView.setImageResource(R.drawable.jcb);
                    break;
                case PayuConstants.SMAE:
                    holder.cardIconImageView.setImageResource(R.drawable.maestro);
                    break;
                default:
                    holder.cardIconImageView.setImageResource(R.drawable.card);
                    break;

            }

            holder.cardNumberTextView.setText(mStoredCards.get(position).getMaskedCardNumber());
            holder.cardNameTextView.setText(mStoredCards.get(position).getCardName());

//            if(mStoredCards.get(position).getEnableOneClickPayment() == 1 && !payuUtils.getFromSharedPreferences(PayUStoredCardsActivity.this, mStoredCards.get(position).getCardToken()).contentEquals(PayuConstants.DEFAULT)){ // The cvv is stored so we can hide the cvv box.
//                holder.cvvEditText.setVisibility(View.GONE);
//                holder.paynNowButton.setEnabled(true);
//            }else if(storeOneClickHash != 0){
//                holder.enableOneClickPaymentCheckBox.setVisibility(View.VISIBLE);
//            }

            if (storeOneClickHash != 0) {
                holder.enableOneClickPaymentCheckBox.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getCount() {
            if (mStoredCards != null)
                return mStoredCards.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int index) {
            if (null != mStoredCards) return mStoredCards.get(index);
            else return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.user_card_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setPosition(position);
            viewHolder(holder, position);

            return convertView;
        }


        class ViewHolder implements View.OnClickListener {

            int position; //for index

            ImageView cardIconImageView;
            ImageView cardTrashImageView;
            TextView cardNumberTextView;
            TextView cardNameTextView;
            ViewGroup cvvPayNowLinearLayout;
            ViewGroup rowLinearLayout;
            Button paynNowButton;
            EditText cvvEditText;
            CheckBox enableOneClickPaymentCheckBox;


            public ViewHolder(View itemView) {

                cardIconImageView = (ImageView) itemView.findViewById(R.id.image_view_card_icon);
                cardNumberTextView = (TextView) itemView.findViewById(R.id.text_view_card_number);
                cardTrashImageView = (ImageView) itemView.findViewById(R.id.image_view_card_trash);
                cardNameTextView = (TextView) itemView.findViewById(R.id.text_view_card_name);
                rowLinearLayout = (ViewGroup) itemView.findViewById(R.id.linear_layout_row);
                cvvPayNowLinearLayout = (ViewGroup) itemView.findViewById(R.id.linear_layout_cvv_paynow);
                paynNowButton = (Button) itemView.findViewById(R.id.button_pay_now);
                cvvEditText = (EditText) itemView.findViewById(R.id.edit_text_cvv);
                enableOneClickPaymentCheckBox = (CheckBox) itemView.findViewById(R.id.check_box_enable_one_click_payment);

                // lets restrict the user not from typing alpha characters.

                cardTrashImageView.setOnClickListener(this);
//                cvvPayNowLinearLayout.setOnClickListener(this);
//                rowLinearLayout.setOnClickListener(this);
                paynNowButton.setOnClickListener(this);

                // we need to set the length of cvv field according to the card number

                cvvEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        /// lets enable or disable the pay now button according to the cvv and card number
                        cvvEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(payuUtils.getIssuer(mStoredCards.get(position).getCardBin()).contentEquals(PayuConstants.AMEX) ? 4 : 3)});
                        if (payuUtils.validateCvv(mStoredCards.get(position).getCardBin(), s.toString())) {
                            paynNowButton.setEnabled(true);
                        } else {
                            paynNowButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                cvvEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                        if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                                actionId == EditorInfo.IME_ACTION_DONE) {
                            onClick(paynNowButton);
                        }
                        return false;
                    }
                });
            }

            public void setPosition(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View view) {
                PayUBaseActivity.hideKeyboard(view.getContext(), view);
                if (cvvPayNowLinearLayout.getVisibility() == View.VISIBLE) {
                    cvvPayNowLinearLayout.setVisibility(View.GONE);
                } else {
                    cvvPayNowLinearLayout.setVisibility(View.VISIBLE);
                }
                if (view.getId() == R.id.image_view_card_trash) {
                    deleteCard(storedCardList.get(position));
                } else if (view.getId() == R.id.button_pay_now) {
                    makePayment(storedCardList.get(position), cvvEditText.getText().toString(), enableOneClickPaymentCheckBox.isChecked());
                }
            }
        }
    }

}


