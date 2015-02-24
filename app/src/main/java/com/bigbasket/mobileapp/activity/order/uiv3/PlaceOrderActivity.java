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
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiResponseContent;
import com.bigbasket.mobileapp.fragment.order.OrderItemListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderSummaryFragment;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.order.VoucherApplied;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PlaceOrderActivity extends BackButtonActivity {

    private String mPotentialOrderId;
    private OrderSummary mOrderSummary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.placeorder));

        mOrderSummary = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPotentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, "");
        renderOrderSummary();

        PayuResponse payuResponse = PayuResponse.getInstance(this);
        if (payuResponse != null && payuResponse.isSuccess()) {
            if (mOrderSummary.getOrderDetails().getFinalTotal() == Double.parseDouble(payuResponse.getAmount())) {
                placeOrder(mPotentialOrderId, payuResponse.getTxnId());
            } else {
                showAlertDialog("Create a separate order?",
                        "We are sorry. The payment amount of Rs." + payuResponse.getAmount() + " does not match the" +
                                " order amount of Rs." + UIUtil.formatAsMoney(mOrderSummary.getOrderDetails().getFinalTotal()) + ". Please go through the " +
                                "payment process to complete this" +
                                " transaction. BigBasket customer service will get back to you regarding " +
                                "the payment made by you.",
                        DialogButton.YES, DialogButton.NO, Constants.SOURCE_POST_PAYMENT
                );
            }
        }
    }

    private void renderOrderSummary() {
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_tab_with_footer_btn, contentView, false);

        contentView.removeAllViews();

        final ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ORDER_REVIEW_SUMMARY, mOrderSummary);

        bbTabs.add(new BBTab<>(getString(R.string.summary), OrderSummaryFragment.class, bundle));
        bbTabs.add(new BBTab<>(getString(R.string.items), OrderItemListFragment.class, bundle));


        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        contentView.addView(base);
        pagerSlidingTabStrip.setViewPager(viewPager);

        Button btnFooter = (Button) base.findViewById(R.id.btnListFooter);
        btnFooter.setTypeface(faceRobotoRegular);
        btnFooter.setText(getString(R.string.placeorder).toUpperCase());
        final HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
        btnFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_CLICKED, map);
                onPlaceOrderAction(mOrderSummary);
            }
        });

        trackEvent(TrackingAware.CHECKOUT_ORDER_REVIEW_SHOWN, map);
    }

    public void onPlaceOrderAction(OrderSummary orderSummary) {

        double amount = orderSummary.getOrderDetails().getFinalTotal();
        PayuResponse existingPayuResponse = PayuResponse.getInstance(getCurrentActivity());
        boolean wasPayuTxnSuccessfulButOrderCreationFailed =
                existingPayuResponse != null && existingPayuResponse.isSuccess();
        if (!wasPayuTxnSuccessfulButOrderCreationFailed &&
                orderSummary.getOrderDetails().getPaymentMethod().equals(Constants.CREDIT_CARD)
                && amount > 0) {
            startCreditCardTxnActivity(amount);
        } else {
            String txnId = wasPayuTxnSuccessfulButOrderCreationFailed ? existingPayuResponse.getTxnId() : null;
            placeOrder(mPotentialOrderId, txnId);
        }
    }

    private void startCreditCardTxnActivity(double amount) {
        Intent intent = new Intent(getApplicationContext(), PayuTransactionActivity.class);
        intent.putExtra(Constants.POTENTIAL_ORDER_ID, mPotentialOrderId);
        intent.putExtra(Constants.FINAL_PAY, new DecimalFormat("0.00").format(amount));
        startActivityForResult(intent, Constants.PAYU_SUCCESS);
    }

    private void placeOrder(String potentialOrderId) {
        placeOrder(potentialOrderId, null);
    }

    private void placeOrder(String potentialOrderId, String txnId) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.placeOrder(potentialOrderId, txnId, new Callback<OldApiResponse<PlaceOrderApiResponseContent>>() {
            @Override
            public void success(OldApiResponse<PlaceOrderApiResponseContent> placeOrderApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (placeOrderApiResponse.status) {
                    case Constants.OK:
                        postOrderCreation(placeOrderApiResponse.apiResponseContent.orders);
                        break;
                    case ApiErrorCodes.AMOUNT_MISMATCH_STR:
                        PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                        String amtTxt = payuResponse != null ? "of Rs. " + payuResponse.getAmount() + " " : "";
                        showAlertDialog("Create a separate order?",
                                "We are sorry. The payment amount " + amtTxt + "does not match the" +
                                        " order amount of Rs." + UIUtil.formatAsMoney(mOrderSummary.getOrderDetails().getFinalTotal()) + ". Please go through the " +
                                        "payment process to complete this" +
                                        " transaction. BigBasket customer service will get back to you regarding " +
                                        "the payment made by you.",
                                DialogButton.YES, DialogButton.NO, Constants.SOURCE_PLACE_ORDER
                        );
                        break;
                    default:
                        handler.sendEmptyMessage(placeOrderApiResponse.getErrorTypeAsInt(), placeOrderApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
        switch (resultCode) {
            case Constants.PAYU_FAILED:
                map.put(TrackEventkeys.FAILURE_REASON, "");
                trackEvent(TrackingAware.CHECKOUT_PAYMENT_GATEWAY_FAILURE, map);
                //TODO: Siddharth while fixing payu, track failure reason
                setResult(resultCode);
                finish();
                break;
            case Constants.PAYU_ABORTED:
                setResult(resultCode);
                finish();
                break;
            case Constants.PAYU_SUCCESS:
                trackEvent(TrackingAware.CHECKOUT_PAYMENT_GATEWAY_SUCCESS, map);
                PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                if (payuResponse == null) {
                    showAlertDialog("Error", "Unable to place your order via credit-card." +
                            "\nPlease choose another method.\n" +
                            "In case your credit card has been charged, " +
                            "BigBasket customer service will get back to you regarding " +
                            "the payment made by you.", Constants.SOURCE_PAYU_EMPTY);
                    setResult(Constants.PAYU_FAILED);
                } else {
                    placeOrder(mPotentialOrderId, payuResponse.getTxnId());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
            // When user clicks the Yes button in Alert Dialog that is shown when there's a amount mismatch
            switch (sourceName) {
                case Constants.SOURCE_PLACE_ORDER:
                    PayuResponse.clearTxnDetail(this);
                    map.put(TrackEventkeys.EXPECTED_AMOUNT, PayuResponse.getInstance(getCurrentActivity()).getAmount());
                    map.put(TrackEventkeys.ORDER_AMOUNT, UIUtil.formatAsMoney(mOrderSummary.getOrderDetails().getFinalTotal()));
                    trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_AMOUNT_MISMATCH, map);
                    if (mOrderSummary.getOrderDetails().getPaymentMethod().equals(Constants.CREDIT_CARD)) {
                        startCreditCardTxnActivity(mOrderSummary.getOrderDetails().getFinalTotal());
                    } else {
                        placeOrder(mPotentialOrderId);
                    }
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
        super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
    }

    private void postOrderCreation(ArrayList<Order> orders) {
        if (orders == null || orders.size() == 0) return;
        for (Order order : orders) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.ORDER_ID, order.getOrderId());
            map.put(TrackEventkeys.ORDER_AMOUNT, order.getOrderValue());
            map.put(TrackEventkeys.ORDER_NUMBER, order.getOrderNumber());
            map.put(TrackEventkeys.ORDER_TYPE, order.getOrderType());
            if (!TextUtils.isEmpty(order.getVoucher()))
                map.put(TrackEventkeys.VOUCHER_NAME, order.getVoucher());
            map.put(TrackEventkeys.PAYMENT_MODE, order.getPaymentMethod());
            map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
            trackEvent(TrackingAware.CHECKOUT_ORDER_COMPLETE, map, null, null, true);
        }

        PayuResponse.clearTxnDetail(this);
        VoucherApplied.clearFromPreference(this);
        removePharmaPrescriptionId();
        ((CartInfoAware) getCurrentActivity()).markBasketDirty();
        showOrderThankyou(orders);
    }

    private void showOrderThankyou(ArrayList<Order> orders) {
        Intent invoiceIntent = new Intent(this, OrderInvoiceActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return null;
    }
}