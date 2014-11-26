package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.order.OrderItemListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderSummaryFragment;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.order.VoucherApplied;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class PlaceOrderActivity extends BackButtonActivity {

    private String potentialOrderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.placeorder));

        OrderSummary orderSummary = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);
        renderOrderSummary(orderSummary);
    }

    private void renderOrderSummary(final OrderSummary orderSummary) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_tab_with_footer_btn, null);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        contentView.removeAllViews();

        final ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ORDER_REVIEW_SUMMARY, orderSummary);

        bbTabs.add(new BBTab<>(getString(R.string.summary), OrderSummaryFragment.class, bundle));
        bbTabs.add(new BBTab<>(getString(R.string.items), OrderItemListFragment.class, bundle));


        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        contentView.addView(base);
        pagerSlidingTabStrip.setViewPager(viewPager);

        Button btnFooter = (Button) base.findViewById(R.id.btnFooter);
        btnFooter.setText(getString(R.string.placeorder));
        btnFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlaceOrderAction(orderSummary);
            }
        });
    }

    public void onPlaceOrderAction(OrderSummary orderSummary) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, "");
        double amount = orderSummary.getOrderDetails().getFinalTotal();
        if (orderSummary.getOrderDetails().getPaymentMethod().equals(Constants.CREDIT_CARD)
                && amount > 0) {
            startCreditCardTxnActivity(amount);
        } else {
            placeOrder(potentialOrderId);
        }
    }

    private void startCreditCardTxnActivity(double amount) {
        Intent intent = new Intent(getApplicationContext(), PayuTransactionActivity.class);
        intent.putExtra(Constants.POTENTIAL_ORDER_ID, potentialOrderId);
        intent.putExtra(Constants.FINAL_PAY, new DecimalFormat("0.00").format(amount));
        startActivityForResult(intent, Constants.PAYU_SUCCESS);
    }

    private void placeOrder(String potentialOrderId) {
        placeOrder(potentialOrderId, null);
    }

    private void placeOrder(String potentialOrderId, String txnId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.P_ORDER_ID, potentialOrderId);
        if (!TextUtils.isEmpty(txnId)) {
            params.put(Constants.TXN_ID, txnId);
        }
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.PLACE_ORDER_URL,
                params, true, AuthParameters.getInstance(this), new BasicCookieStore());
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.PLACE_ORDER_URL)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = responseJsonObj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    JsonObject response = responseJsonObj.get(Constants.RESPONSE).getAsJsonObject();
                    JsonArray ordersJsonArray = response.get(Constants.ORDERS).getAsJsonArray();
                    ArrayList<Order> orders = ParserUtil.parseOrderList(ordersJsonArray);
                    postOrderCreation(orders);
                    break;
                default:
                    // TODO : Add error handling
                    showAlertDialog(this, null, "Server Error");
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        switch (resultCode) {
            case Constants.PAYU_FAILED:
                setResult(resultCode);
                finish();
                break;
            case Constants.PAYU_ABORTED:
                setResult(resultCode);
                finish();
                break;
            case Constants.PAYU_SUCCESS:
                PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                if (payuResponse == null) {
                    showAlertDialog(this, "Error", "Unable to place your order via credit-card." +
                            "\nPlease choose another method.\n" +
                            "In case your credit card has been charged, " +
                            "BigBasket customer service will get back to you regarding " +
                            "the payment made by you.", Constants.SOURCE_PAYU_EMPTY);
                    setResult(Constants.PAYU_FAILED);
                } else {
                    placeOrder(potentialOrderId, payuResponse.getTxnId());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            // When user clicks the Yes button in Alert Dialog that is shown when there's a amount mismatch
            switch (sourceName) {
                case Constants.SOURCE_PLACE_ORDER:
                    PayuResponse.clearTxnDetail(this);
//                    if (paymethod.equals(Constants.CREDIT_CARD)) {
//                        startCreditCardTxnActivity();
//                    } else {
//                        callWebservicePlaceOrder(MobileApiUrl.getBaseAPIUrl() + Constants.PLACE_ORDER_URL, null);
//                    }
                    break;
                case Constants.SOURCE_POST_PAYMENT:
                    PayuResponse.clearTxnDetail(this);
                    finish();
                    break;
                case Constants.SOURCE_PAYU_EMPTY:
                    finish();
                    break;
            }
        }
        super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
    }

    private void postOrderCreation(ArrayList<Order> orders) {
        PayuResponse.clearTxnDetail(this);
        VoucherApplied.clearFromPreference(this);
        showOrderThankyou(orders);
        finish();
    }

    private void showOrderThankyou(ArrayList<Order> orders) {
        Intent invoiceIntent = new Intent(this, OrderInvoiceActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        startActivityForResult(invoiceIntent, Constants.GO_TO_HOME);
    }
}