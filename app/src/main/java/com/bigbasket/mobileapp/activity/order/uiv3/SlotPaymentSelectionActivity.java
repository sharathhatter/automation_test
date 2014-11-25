package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
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
import com.bigbasket.mobileapp.fragment.order.PaymentSelectionFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.SelectedSlotAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.slot.SelectedSlotType;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SlotPaymentSelectionActivity extends BackButtonActivity
        implements SelectedSlotAware, SelectedPaymentAware {

    private ArrayList<SelectedSlotType> mSelectedSlotType;
    private String mPaymentMethod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Checkout");
        loadSlotsAndPayments();
    }

    private void loadSlotsAndPayments() {
        String addressId = getIntent().getStringExtra(Constants.ADDRESS_ID);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);

        String url = MobileApiUrl.getBaseAPIUrl() + Constants.POST_DELIVERY_ADDR;
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.P_ORDER_ID, potentialOrderId);
        params.put(Constants.ADDRESS_ID, addressId);
        params.put(Constants.SUPPORT_CC, "yes");
        startAsyncActivity(url, params, true, AuthParameters.getInstance(this), new BasicCookieStore());
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.POST_DELIVERY_ADDR)) {
            try {
                JSONObject httpResponseJsonObj = new JSONObject(httpOperationResult.getReponseString());
                String status = httpResponseJsonObj.getString(Constants.STATUS);
                switch (status) {
                    case Constants.OK:
                        JSONObject responseJsonObj = httpResponseJsonObj.getJSONObject(Constants.RESPONSE);
                        JSONArray slotsInfo = responseJsonObj.getJSONArray(Constants.SLOTS_INFO);
                        ArrayList<SlotGroup> slotGroupList = ParserUtil.parseSlotsList(slotsInfo);

                        JSONObject cartSummaryJsonObj = responseJsonObj.getJSONObject(Constants.CART_SUMMARY);
                        CartSummary cartSummary = ParserUtil.parseCartSummaryFromJSON(cartSummaryJsonObj);

                        String amtPayable = cartSummaryJsonObj.getString(Constants.AMT_PAYABLE);
                        String walletUsed = cartSummaryJsonObj.getString(Constants.WALLET_USED);
                        String walletRemaining = cartSummaryJsonObj.getString(Constants.WALLET_REMAINING);
                        JSONArray activeVouchersJsonArray = responseJsonObj.optJSONArray(Constants.VOUCHERS);
                        ArrayList<ActiveVouchers> activeVouchersList = new ArrayList<>();
                        if (activeVouchersJsonArray != null && activeVouchersJsonArray.length() > 0) {
                            activeVouchersList = ParserUtil.parseActiveVouchersList(activeVouchersJsonArray);
                        }
                        JSONArray paymentTypesJsonArray = responseJsonObj.getJSONArray(Constants.PAYMENT_TYPES);
                        setTabs(slotGroupList, cartSummary, amtPayable, walletUsed, walletRemaining,
                                activeVouchersList, paymentTypesJsonArray);
                        break;
                    case Constants.ERROR:
                        // TODO : Change this later on
                        showAlertDialog("Server Error");
                        break;
                }
            } catch (JSONException ex) {
                // TODO : Change this later on
                showAlertDialog("Invalid response");
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void setTabs(ArrayList<SlotGroup> slotGroupList, CartSummary cartSummary,
                         String amtPayable, String walletUsed, String walletRemaining,
                         ArrayList<ActiveVouchers> activeVouchersList,
                         JSONArray paymentTypesJsonArray) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_slot_payment_layout, null);

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
        paymentSelectionBundle.putString(Constants.PAYMENT_TYPES, paymentTypesJsonArray.toString());
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
                launchOrderReview();
            }
        });
    }

    private void launchOrderReview() {
        if (mSelectedSlotType == null || mSelectedSlotType.size() == 0) {
            showAlertDialog(this, null, getString(R.string.selectAllSlotsErrMsg));
            return;
        }
        if (TextUtils.isEmpty(mPaymentMethod)) {
            showAlertDialog(this, null, getString(R.string.pleaseChoosePaymentMethod));
            return;
        }
    }

    @Override
    public void setSelectedSlotType(ArrayList<SelectedSlotType> selectedSlotType) {
        this.mSelectedSlotType = selectedSlotType;
    }

    @Override
    public void setPaymentMethod(String paymentMethod) {
        this.mPaymentMethod = paymentMethod;
    }
}
