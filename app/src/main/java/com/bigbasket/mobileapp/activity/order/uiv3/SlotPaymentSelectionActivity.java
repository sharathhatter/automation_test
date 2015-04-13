package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostDeliveryAddressCartSummary;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponse;
import com.bigbasket.mobileapp.fragment.order.PaymentSelectionFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.interfaces.OnApplyVoucherListener;
import com.bigbasket.mobileapp.interfaces.PostVoucherAppliedListener;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.SelectedSlotAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.order.PowerPayResponse;
import com.bigbasket.mobileapp.model.order.VoucherApplied;
import com.bigbasket.mobileapp.model.slot.SelectedSlotType;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SlotPaymentSelectionActivity extends BackButtonActivity
        implements SelectedSlotAware, SelectedPaymentAware,
        OnApplyVoucherListener {

    private ArrayList<SelectedSlotType> mSelectedSlotType;
    private String mPaymentMethodSlug;
    private String mPotentialOrderId;
    private ArrayList<VoucherApplied> mVoucherAppliedList;
    private HashMap<String, Boolean> mPreviouslyAppliedVoucherMap;
    private String mCreditCardTxnFailureReason;
    private boolean mIsVoucherInProgress;
    private Button mBtnFooter;
    private boolean mHasSlotTabLaunchedOnce;
    private boolean mHasSlotExpired;
    private boolean mPrevTxnPending;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.payment_and_slot_selection));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHasSlotTabLaunchedOnce = false;

        if (!mIsVoucherInProgress) {
            loadSlotsAndPayments();
        }
    }

    private void loadSlotsAndPayments() {
        String addressId = getIntent().getStringExtra(Constants.ADDRESS_ID);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPotentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postDeliveryAddresses(mPotentialOrderId, addressId, "yes", "yes",
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
                                        postDeliveryAddressApiResponse.apiResponseContent.paymentTypes,
                                        postDeliveryAddressApiResponse.apiResponseContent.evoucherCode,
                                        postDeliveryAddressApiResponse.apiResponseContent.slotMessage);
                                break;
                            case Constants.ERROR:
                                handler.sendEmptyMessage(postDeliveryAddressApiResponse.getErrorTypeAsInt(),
                                        postDeliveryAddressApiResponse.message);
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
                         ArrayList<PaymentType> paymentTypes,
                         String voucherCode, String slotMsg) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        View base = inflater.inflate(R.layout.uiv3_tab_with_footer_btn, contentView, false);

        contentView.removeAllViews();

        final ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle slotBundle = new Bundle();
        slotBundle.putParcelableArrayList(Constants.SLOTS_INFO, slotGroupList);
        if (!TextUtils.isEmpty(slotMsg)) {
            slotBundle.putString(Constants.SLOT_MESSAGE, slotMsg);
        }

        Bundle paymentSelectionBundle = new Bundle();
        paymentSelectionBundle.putParcelable(Constants.C_SUMMARY, cartSummary);
        paymentSelectionBundle.putString(Constants.AMT_PAYABLE, amtPayable);
        paymentSelectionBundle.putString(Constants.WALLET_USED, walletUsed);
        paymentSelectionBundle.putString(Constants.WALLET_REMAINING, walletRemaining);
        paymentSelectionBundle.putParcelableArrayList(Constants.VOUCHERS, activeVouchersList);
        paymentSelectionBundle.putParcelableArrayList(Constants.PAYMENT_TYPES, paymentTypes);
        if (!TextUtils.isEmpty(voucherCode)) {
            paymentSelectionBundle.putString(Constants.EVOUCHER_CODE, voucherCode);
        }

        boolean hasCreditCardTxnFailed = !TextUtils.isEmpty(mCreditCardTxnFailureReason);
        if (hasCreditCardTxnFailed) {
            paymentSelectionBundle.putString(Constants.PAYU_CANCELLED, mCreditCardTxnFailureReason);
        }
        bbTabs.add(new BBTab<>(getString(R.string.paymentMethod), PaymentSelectionFragment.class, paymentSelectionBundle));
        bbTabs.add(new BBTab<>(getString(R.string.slot), SlotSelectionFragment.class, slotBundle));

        final ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);


        mBtnFooter = (Button) base.findViewById(R.id.btnListFooter);
        mBtnFooter.setTypeface(faceRobotoRegular);
        mBtnFooter.setText(getString(R.string.chooseSlot).toUpperCase());

        if (!hasCreditCardTxnFailed) {
            PayuResponse payuResponse = PayuResponse.getInstance(this);
            PowerPayResponse powerPayResponse = PowerPayResponse.getInstance(this);
            if (payuResponse != null && payuResponse.isSuccess()) {
                mPrevTxnPending = true;
                mPaymentMethodSlug = Constants.PAYU;
            } else if (powerPayResponse != null && powerPayResponse.isSuccess()) {
                mPrevTxnPending = true;
                mPaymentMethodSlug = Constants.HDFC_POWER_PAY;
            }
            if (mHasSlotExpired || mPrevTxnPending) {
                viewPager.setCurrentItem(1);
            }
            if (mPrevTxnPending) {
                mBtnFooter.setText(getString(R.string.placeorder).toUpperCase());
            }
        }

        contentView.addView(base);
        final PagerSlidingTabStrip slidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        slidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    mHasSlotTabLaunchedOnce = true;
                    if (!mPrevTxnPending) {
                        onSlotNavigation();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        slidingTabStrip.setViewPager(viewPager);

        mBtnFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHasSlotExpired = false;
                PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                PowerPayResponse powerPayResponse = PowerPayResponse.getInstance(getCurrentActivity());
                if ((payuResponse != null && payuResponse.isSuccess()) || (powerPayResponse != null && powerPayResponse.isSuccess())) {
                    ArrayList<VoucherApplied> previouslyAppliedVoucherList = VoucherApplied.readFromPreference(getCurrentActivity());
                    if (previouslyAppliedVoucherList == null || previouslyAppliedVoucherList.size() == 0) {
                        launchOrderReview();
                    } else {
                        mPreviouslyAppliedVoucherMap = VoucherApplied.toMap(previouslyAppliedVoucherList);
                        applyVoucher(previouslyAppliedVoucherList.get(0).getVoucherCode());
                    }
                } else {
                    if (!mHasSlotTabLaunchedOnce) {
                        viewPager.setCurrentItem(1, true);
                    } else {
                        launchOrderReview();
                    }
                }
            }
        });
    }

    private void logSlotPaymentEvent(String potentialOrderId) {


        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.POTENTIAL_ORDER, potentialOrderId);
        map.put(TrackEventkeys.PAYMENT_MODE, mPaymentMethodSlug);
        trackEvent(TrackingAware.CHECKOUT_ORDER_REVIEW_CLICKED, map);
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
        logSlotPaymentEvent(potentialOrderId);

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
                                launchPlaceOrderActivity(orderSummary);
                                break;
                            default:
                                handler.sendEmptyMessage(orderSummaryApiResponse.status,
                                        orderSummaryApiResponse.message);
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
    public void setPaymentMethod(String paymentMethodSlug) {
        this.mPaymentMethodSlug = paymentMethodSlug;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        switch (resultCode) {
            case NavigationCodes.GO_TO_SLOT_SELECTION:
                mHasSlotExpired = true;
                mHasSlotTabLaunchedOnce = false;
                break;
            case Constants.PREPAID_TXN_ABORTED:
                mCreditCardTxnFailureReason = getString(R.string.youAborted);
                break;
            case Constants.PREPAID_TXN_FAILED:
                mCreditCardTxnFailureReason = getString(R.string.failedToProcess);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCreditCardTxnFailureReason = null;
    }

    @Override
    public void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            mIsVoucherInProgress = true;
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.postVoucher(mPotentialOrderId, voucherCode, new Callback<PostVoucherApiResponse>() {
                @Override
                public void success(PostVoucherApiResponse postVoucherApiResponse, Response response) {
                    mIsVoucherInProgress = false;
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
                    map.put(TrackEventkeys.VOUCHER_NAME, voucherCode);
                    switch (postVoucherApiResponse.status) {
                        case Constants.OK:
                            if (mPreviouslyAppliedVoucherMap == null ||
                                    mPreviouslyAppliedVoucherMap.size() == 0) {
                                trackEvent(TrackingAware.CHECKOUT_VOUCHER_APPLIED, map);
                            }
                            onVoucherSuccessfullyApplied(voucherCode);
                            break;
                        default:
                            handler.sendEmptyMessage(postVoucherApiResponse.getErrorTypeAsInt(), postVoucherApiResponse.message);
                            map.put(TrackEventkeys.FAILURE_REASON, postVoucherApiResponse.message);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, map);
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    mIsVoucherInProgress = false;
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    handler.handleRetrofitError(error);
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
                }
            });
        } else {
            handler.sendOfflineError();
        }
    }

    @Override
    public void removeVoucher(final String voucherCode) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.removeVoucher(mPotentialOrderId, new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse removeVoucherApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (removeVoucherApiResponse.status) {
                    case 0:
                        Toast.makeText(getCurrentActivity(),
                                getString(R.string.voucherWasRemoved), Toast.LENGTH_SHORT).show();
                        onVoucherRemoved(voucherCode);
                        break;
                    default:
                        handler.sendEmptyMessage(removeVoucherApiResponse.status,
                                removeVoucherApiResponse.message);
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

    /**
     * Callback when the voucher has been successfully applied
     */
    private void onVoucherSuccessfullyApplied(String voucherCode) {
        if (mPreviouslyAppliedVoucherMap != null) {
            mPreviouslyAppliedVoucherMap.put(voucherCode, true);
            boolean allApplied = true;
            for (Map.Entry<String, Boolean> entry : mPreviouslyAppliedVoucherMap.entrySet()) {
                if (!entry.getValue()) {
                    allApplied = false;
                    applyVoucher(entry.getKey());
                    break;
                }
            }
            if (allApplied) {
                launchOrderReview();
            }
        } else {
            if (mVoucherAppliedList == null) {
                mVoucherAppliedList = new ArrayList<>();
            }
            mVoucherAppliedList.add(new VoucherApplied(voucherCode));
            VoucherApplied.saveToPreference(mVoucherAppliedList, this);
            for (Fragment fg : getSupportFragmentManager().getFragments()) {
                if (fg instanceof PostVoucherAppliedListener) {
                    ((PostVoucherAppliedListener) fg).onVoucherApplied(voucherCode);
                }
            }
        }
    }

    private void onVoucherRemoved(String voucherCode) {
        if (mPreviouslyAppliedVoucherMap != null
                && mPreviouslyAppliedVoucherMap.containsKey(voucherCode)) {
            mPreviouslyAppliedVoucherMap.remove(voucherCode);
        }
        for (Fragment fg : getSupportFragmentManager().getFragments()) {
            if (fg instanceof PostVoucherAppliedListener) {
                ((PostVoucherAppliedListener) fg).onVoucherRemoved();
            }
        }
    }

    @Override
    public String getScreenTag() {
        return null;
    }

    public void onSlotNavigation() {
        mBtnFooter.setText(getString(R.string.orderReview).toUpperCase());
    }
}
