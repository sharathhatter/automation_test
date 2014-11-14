package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.AvailableVoucherListActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.PlaceOrderActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.slot.SelectedSlotType;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class PaymentSelectionFragment extends BaseFragment {

    private CartSummary cartSummary;
    private ArrayList<SlotGroup> selectedSlotGroupList;
    private ArrayList<ActiveVouchers> activeVouchersList;
    private LinkedHashMap<String, String> paymentTypeMap;
    private String amtPayable, walletUsed, walletRemaining;
    private String potentialOrderId;
    private Spinner spinnerPaymentMethod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, null);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadPaymentMethods();
    }

    private void loadPaymentMethods() {
        ArrayList<SelectedSlotType> selectedSlotTypes = getArguments().getParcelableArrayList(Constants.SLOTS);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);
        Gson gson = new Gson();
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.P_ORDER_ID, potentialOrderId);
        params.put(Constants.SLOTS, gson.toJson(selectedSlotTypes));
        params.put(Constants.SUPPORT_CC, "yes");
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.CO_POST_SLOTS;
        startAsyncActivity(url, params, true, true, null);
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.CO_POST_SLOTS)) {
            try {
                JSONObject jsonObject = new JSONObject(httpOperationResult.getReponseString());
                String status = jsonObject.getString(Constants.STATUS);
                switch (status) {
                    case Constants.OK:
                        JSONObject responseJsonObj = jsonObject.getJSONObject(Constants.RESPONSE);
                        JSONObject cartSummaryJsonObj = responseJsonObj.getJSONObject(Constants.CART_SUMMARY);
                        cartSummary = ParserUtil.parseCartSummaryFromJSON(cartSummaryJsonObj);
                        JSONArray slotsInfo = responseJsonObj.getJSONArray(Constants.SLOTS_INFO);
                        selectedSlotGroupList = ParserUtil.parseSlotsList(slotsInfo);
                        amtPayable = cartSummaryJsonObj.getString(Constants.AMT_PAYABLE);
                        walletUsed = cartSummaryJsonObj.getString(Constants.WALLET_USED);
                        walletRemaining = cartSummaryJsonObj.getString(Constants.WALLET_REMAINING);
                        JSONArray activeVouchersJsonArray = responseJsonObj.optJSONArray(Constants.VOUCHERS);
                        if (activeVouchersJsonArray != null && activeVouchersJsonArray.length() > 0) {
                            activeVouchersList = ParserUtil.parseActiveVouchersList(activeVouchersJsonArray);
                        }
                        JSONArray paymentTypesJsonArray = responseJsonObj.getJSONArray(Constants.PAYMENT_TYPES);
                        paymentTypeMap = ParserUtil.parsePaymentTypes(paymentTypesJsonArray);
                        renderPaymentOptions();
                        break;
                    default:
                        showErrorMsg("Server Error");
                        // TODO : Improve error handling
                        break;
                }
            } catch (JSONException e) {
                // TODO : Improve error handling
                showErrorMsg("Server Error");
            }
        } else if (url.contains(Constants.CO_POST_VOUCHER)) {
            try {
                JSONObject jsonObject = new JSONObject(httpOperationResult.getReponseString());
                String status = jsonObject.getString(Constants.STATUS);
                switch (status) {
                    case Constants.OK:
                        // TODO : Add previous applied voucher handling logic for credit card
                        String voucherMsg;
                        if (jsonObject.has(Constants.EVOUCHER_MSG) &&
                                !TextUtils.isEmpty(jsonObject.getString(Constants.EVOUCHER_MSG))) {
                            voucherMsg = jsonObject.getString(Constants.EVOUCHER_MSG);
                        } else {
                            voucherMsg = "eVoucher has been successfully applied";
                        }
                        showErrorMsg(voucherMsg); // TODO : Change this
                        break;
                    default:
                        String msg = jsonObject.getString(Constants.MESSAGE);
                        showErrorMsg(msg);
                        break;
                }
            } catch (JSONException e) {
                // TODO : Improve error handling
                showErrorMsg("Server Error");
            }
        } else if (url.contains(Constants.CO_POST_PAYMENT)) {
            JsonObject jsonObject = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    JsonObject responseJsonObj = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                    OrderSummary orderSummary = ParserUtil.parseOrderSummary(responseJsonObj);
                    HashMap<Object, String> additionalCtx = httpOperationResult.getAdditionalCtx();
                    String paymentMethodDisplay = additionalCtx.get(Constants.PAYMENT_METHOD_DISPLAY);
                    orderSummary.getOrderDetails().setPaymentMethodDisplay(paymentMethodDisplay);
                    launchPlaceOrderActivity(orderSummary);
                    break;
                default:
                    // TODO : Add error handling
                    showErrorMsg("Server Error");
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void renderPaymentOptions() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_payment_option, null);

        spinnerPaymentMethod = (Spinner) base.findViewById(R.id.spinnerPaymentMethod);
        ArrayList<String> paymentMethodsDisplayList = new ArrayList<>(paymentTypeMap.keySet());
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                        paymentMethodsDisplayList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(spinnerAdapter);
        View viewPaymentOptionSeparator = base.findViewById(R.id.viewPaymentOptionSeparator);
        if (Double.parseDouble(amtPayable) <= 0 && Double.parseDouble(walletUsed) > 0) {
            // TODO : Plugin in wallet handling
        } else {
            viewPaymentOptionSeparator.setVisibility(View.GONE);
        }

        TextView lblPaymentMethod = (TextView) base.findViewById(R.id.lblPaymentMethod);
        lblPaymentMethod.setTypeface(faceRobotoRegular);

        TextView txtApplyVoucher = (TextView) base.findViewById(R.id.txtApplyVoucher);
        final EditText editTextVoucherCode = (EditText) base.findViewById(R.id.editTextVoucherCode);
        txtApplyVoucher.setTypeface(faceRobotoRegular);
        txtApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyVoucher(editTextVoucherCode.getText().toString());
            }
        });

        TextView txtViewAvailableVouchers = (TextView) base.findViewById(R.id.txtViewAvailableVouchers);
        if (activeVouchersList != null && activeVouchersList.size() > 0) {
            txtViewAvailableVouchers.setTypeface(faceRobotoRegular);
            txtViewAvailableVouchers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent availableVoucherListActivity = new Intent(getActivity(), AvailableVoucherListActivity.class);
                    availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, activeVouchersList);
                    startActivityForResult(availableVoucherListActivity, Constants.VOUCHER_APPLIED);
                }
            });
        } else {
            txtViewAvailableVouchers.setVisibility(View.GONE);
        }

        LinearLayout layoutDeliverySlot = (LinearLayout) base.findViewById(R.id.layoutDeliverySlot);
        renderSlots(layoutDeliverySlot);
        contentView.addView(base);

        TextView txtReviewOrder = (TextView) base.findViewById(R.id.txtReviewOrder);
        txtReviewOrder.setTypeface(faceRobotoThin);
        txtReviewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPostPayment();
            }
        });
    }

    private void renderSlots(LinearLayout layoutDeliverySlot) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        boolean hasMultipleSlots = selectedSlotGroupList.size() > 1;
        for (SlotGroup slotGroup : selectedSlotGroupList) {
            View row = inflater.inflate(R.layout.uiv3_slot_info_row, null);
            renderSlotInfoRow(row, slotGroup, hasMultipleSlots);
            layoutDeliverySlot.addView(row);
        }
    }

    private void renderSlotInfoRow(View row, SlotGroup slotGroup, boolean hasMultipleSlots) {
        if (getActivity() == null || getBaseActivity() == null) return;
        TextView txtNumItems = (TextView) row.findViewById(R.id.txtNumItems);
        TextView txtBasketVal = (TextView) row.findViewById(R.id.txtBasketVal);
        TextView txtSlotDate = (TextView) row.findViewById(R.id.txtSlotDate);
        TextView txtSlotTime = (TextView) row.findViewById(R.id.txtSlotTime);
        TextView txtFulfilledBy = (TextView) row.findViewById(R.id.txtFulfilledBy);
        txtBasketVal.setTypeface(faceRobotoRegular);
        txtFulfilledBy.setTypeface(faceRobotoRegular);
        txtSlotDate.setTypeface(faceRobotoRegular);
        txtNumItems.setTypeface(faceRobotoRegular);
        txtSlotTime.setTypeface(faceRobotoRegular);

        txtSlotDate.setText(slotGroup.getSelectedSlot().getFormattedSlotDate());
        txtSlotTime.setText(slotGroup.getSelectedSlot().getDisplayName());
        if (hasMultipleSlots) {
            txtNumItems.setVisibility(View.GONE);
            txtBasketVal.setVisibility(View.GONE);
            txtFulfilledBy.setText(slotGroup.getFulfillmentInfo().getFulfilledBy());
        } else {
            int numItems = cartSummary.getNoOfItems();
            txtNumItems.setText(numItems + "Item" + (numItems > 1 ? "s" : ""));
            txtBasketVal.setText(getBaseActivity().
                    asRupeeSpannable(getBaseActivity().getDecimalAmount(cartSummary.getTotal())));
            txtFulfilledBy.setVisibility(View.GONE);
        }
    }

    private void applyVoucher(String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            String url = MobileApiUrl.getBaseAPIUrl() + Constants.CO_POST_VOUCHER;
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.P_ORDER_ID, potentialOrderId);
            params.put(Constants.EVOUCHER_CODE, voucherCode);
            startAsyncActivity(url, params, true, false, null);
        } else {
            showErrorMsg(getString(R.string.connectionOffline));
        }
    }

    private void startPostPayment() {
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.CO_POST_PAYMENT;
        String paymentMethodSlug = paymentTypeMap.get(spinnerPaymentMethod.getSelectedItem().toString());
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.P_ORDER_ID, potentialOrderId);
        params.put(Constants.PAYMENT_TYPE, paymentMethodSlug);
        HashMap<Object, String> additionalCtx = new HashMap<>();
        additionalCtx.put(Constants.PAYMENT_METHOD_DISPLAY, spinnerPaymentMethod.getSelectedItem().toString());
        startAsyncActivity(url, params, true, false, additionalCtx);
    }

    private void launchPlaceOrderActivity(OrderSummary orderSummary) {
        Intent placeOrderIntent = new Intent(getActivity(), PlaceOrderActivity.class);
        placeOrderIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderSummary);
        startActivityForResult(placeOrderIntent, Constants.ORDER_COMPLETE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setFragmentSuspended(false);
        if (resultCode == Constants.VOUCHER_APPLIED && data != null) {
            String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
            if (!TextUtils.isEmpty(voucherCode)) {
                applyVoucher(voucherCode);
            }
        } else if (resultCode == Constants.ORDER_COMPLETE && data != null) {
            ArrayList<Order> orders = data.getParcelableArrayListExtra(Constants.ORDERS);
            showOrderThankyou(orders);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showOrderThankyou(ArrayList<Order> orders) {
        OrderThankYouFragment orderThankYouFragment = new OrderThankYouFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.ORDERS, orders);
        orderThankYouFragment.setArguments(bundle);
        changeFragment(orderThankYouFragment);
    }

    @Override
    public String getTitle() {
        return "Choose Payment Mode";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PaymentSelectionFragment.class.getName();
    }
}