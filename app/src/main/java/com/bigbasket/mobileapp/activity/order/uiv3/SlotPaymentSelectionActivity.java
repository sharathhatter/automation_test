package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
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
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostDeliveryAddressCartSummary;
import com.bigbasket.mobileapp.fragment.order.PaymentSelectionFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.SelectedSlotAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.slot.SelectedSlotType;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SlotPaymentSelectionActivity extends BackButtonActivity
        implements SelectedSlotAware, SelectedPaymentAware {

    private ArrayList<SelectedSlotType> mSelectedSlotType;
    private String mPaymentMethodSlug;
    private String mPaymentMethodDisplay;
    private SharedPreferences preferences;
    private String potentialOrderId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Checkout");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSlotsAndPayments();
    }

    private void loadSlotsAndPayments() {
        String addressId = getIntent().getStringExtra(Constants.ADDRESS_ID);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postDeliveryAddresses(potentialOrderId, addressId, "yes",
                new Callback<OldApiResponse<PostDeliveryAddressApiResponseContent>>() {
                    @Override
                    public void success(OldApiResponse<PostDeliveryAddressApiResponseContent> postDeliveryAddressApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (postDeliveryAddressApiResponse.status) {
                            case Constants.OK:
                                PostDeliveryAddressCartSummary postDeliveryAddressCartSummary =
                                        postDeliveryAddressApiResponse.apiResponseContent.cartSummary;
                                CartSummary cartSummary = new CartSummary(postDeliveryAddressCartSummary.getTotal(),
                                        postDeliveryAddressCartSummary.getSavings(), postDeliveryAddressCartSummary.getNoOfItems());

                                setTabs(postDeliveryAddressApiResponse.apiResponseContent.slotGroupArrayList,
                                        cartSummary, postDeliveryAddressCartSummary.amtPayable, postDeliveryAddressCartSummary.walletUsed,
                                        postDeliveryAddressCartSummary.walletRemaining,
                                        postDeliveryAddressApiResponse.apiResponseContent.activeVouchersArrayList,
                                        postDeliveryAddressApiResponse.apiResponseContent.paymentTypes);
                                break;
                            case Constants.ERROR:
                                handler.sendEmptyMessage(postDeliveryAddressApiResponse.getErrorTypeAsInt());
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

    private void launchPlaceOrderActivity(OrderSummary orderSummary) {
        Intent intent = new Intent(this, PlaceOrderActivity.class);
        intent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderSummary);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private void setTabs(ArrayList<SlotGroup> slotGroupList, CartSummary cartSummary,
                         String amtPayable, String walletUsed, String walletRemaining,
                         ArrayList<ActiveVouchers> activeVouchersList,
                         ArrayList<PaymentType> paymentTypes) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_tab_with_footer_btn, null);

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        contentView.removeAllViews();

        final ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle slotBundle = new Bundle();
        slotBundle.putParcelableArrayList(Constants.SLOTS_INFO, slotGroupList);
        bbTabs.add(new BBTab<>("Slot", SlotSelectionFragment.class, slotBundle));

        Bundle paymentSelectionBundle = new Bundle();
        paymentSelectionBundle.putParcelable(Constants.C_SUMMARY, cartSummary);
        paymentSelectionBundle.putString(Constants.AMT_PAYABLE, amtPayable);
        paymentSelectionBundle.putString(Constants.WALLET_USED, walletUsed);
        paymentSelectionBundle.putString(Constants.WALLET_REMAINING, walletRemaining);
        paymentSelectionBundle.putParcelableArrayList(Constants.VOUCHERS, activeVouchersList);
        paymentSelectionBundle.putParcelableArrayList(Constants.PAYMENT_TYPES, paymentTypes);
        bbTabs.add(new BBTab<>("Payment Method", PaymentSelectionFragment.class, paymentSelectionBundle));

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        contentView.addView(base);
        pagerSlidingTabStrip.setViewPager(viewPager);

        Button btnFooter = (Button) base.findViewById(R.id.btnFooter);
        btnFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.POTENTIAL_ORDER, potentialOrderId);
                map.put(TrackEventkeys.PAYMENT_MODE, preferences.getString(Constants.PAYMENT_METHOD, ""));
                trackEvent(TrackingAware.CHECKOUT_PAYMENT_CHOSEN, map);
                launchOrderReview();
            }
        });
    }

    private void launchOrderReview() {
        if (mSelectedSlotType == null || mSelectedSlotType.size() == 0) {
            showAlertDialog(null, getString(R.string.selectAllSlotsErrMsg));
            return;
        }
        if (TextUtils.isEmpty(mPaymentMethodSlug)) {
            showAlertDialog(null, getString(R.string.pleaseChoosePaymentMethod));
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postSlotAndPayment(potentialOrderId, new Gson().toJson(mSelectedSlotType),
                mPaymentMethodSlug, "yes", new Callback<ApiResponse<OrderSummary>>() {
                    @Override
                    public void success(ApiResponse<OrderSummary> orderSummaryApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (orderSummaryApiResponse.status) {
                            case 0:
                                OrderSummary orderSummary = orderSummaryApiResponse.apiResponseContent;
                                orderSummary.getOrderDetails().setPaymentMethodDisplay(mPaymentMethodDisplay);
                                launchPlaceOrderActivity(orderSummary);
                                break;
                            default:
                                handler.sendEmptyMessage(orderSummaryApiResponse.status);
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
    public void setSelectedSlotType(ArrayList<SelectedSlotType> selectedSlotType) {
        this.mSelectedSlotType = selectedSlotType;
    }

    @Override
    public void setPaymentMethod(String paymentMethodSlug, String paymentMethodDisplay) {
        this.mPaymentMethodSlug = paymentMethodSlug;
        this.mPaymentMethodDisplay = paymentMethodDisplay;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != NavigationCodes.GO_TO_SLOT_SELECTION) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
