package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderInvoiceDetails;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;
import java.util.Map;


public class OrderInvoiceSummaryFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadOrderInvoiceSummary();
    }

    private void loadOrderInvoiceSummary() {
        OrderInvoice orderInvoice;
        if (getArguments() != null) {
            orderInvoice = getArguments().getParcelable(Constants.ACTION_TAB_TAG);
            setTitle("Order Details");
            renderOrderInvoice(orderInvoice);
        }
    }

    private void renderOrderInvoice(final OrderInvoice orderInvoice) {
        if (getActivity() == null) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        showProgressView();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_order_invoice_summary, contentView, false);

        // Render slots
        LinearLayout layoutDeliverySlot = (LinearLayout) base.findViewById(R.id.layoutDeliverySlot);
        View slotInfoRow = inflater.inflate(R.layout.uiv3_slot_info_row, layoutDeliverySlot, false);
        String[] slotDateAndTime = orderInvoice.getSlot().getDisplayName().split("between");
        renderSlotInfoRow(slotInfoRow, slotDateAndTime[0].trim(), slotDateAndTime[1].trim(),
                orderInvoice.getFulfillmentInfo().getFulfilledBy(), false);
        layoutDeliverySlot.addView(slotInfoRow);

        // Show delivered time
        TextView txtOrderStatus = (TextView) base.findViewById(R.id.txtOrderStatus);
        txtOrderStatus.setTypeface(faceRobotoRegular);
        txtOrderStatus.setText("Order status: " + orderInvoice.getOrderInvoiceDetails().getOrderStatus());

        // Show Member Details
        TextView txtMemberName = (TextView) base.findViewById(R.id.txtMemberName);
        txtMemberName.setTypeface(faceRobotoRegular);
        txtMemberName.setText(orderInvoice.getMemberSummary().getMemberName());

        TextView txtMemberAddress = (TextView) base.findViewById(R.id.txtMemberAddress);
        txtMemberAddress.setTypeface(faceRobotoRegular);
        txtMemberAddress.setText(orderInvoice.getMemberSummary().getAddress());

        TextView txtMemberContactNum = (TextView) base.findViewById(R.id.txtMemberContactNum);
        if (TextUtils.isEmpty(orderInvoice.getMemberSummary().getMobile())) {
            txtMemberContactNum.setVisibility(View.GONE);
        } else {
            txtMemberContactNum.setTypeface(faceRobotoRegular);
            txtMemberContactNum.setText(orderInvoice.getMemberSummary().getMobile());
        }

        // Show order & invoice details
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);

        LinearLayout layoutOrderSummaryInfo = (LinearLayout) base.findViewById(R.id.layoutOrderSummaryInfo);
        OrderInvoiceDetails orderInvoiceDetails = orderInvoice.getOrderInvoiceDetails();

        View orderNumberRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.ordernumber),
                orderInvoice.getOrderNumber(), normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderNumberRow);

        View invoiceNumberRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.invoicenumber),
                orderInvoice.getInvoiceNumber(), normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(invoiceNumberRow);

        View paymentMethodRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.paymentMethod),
                orderInvoiceDetails.getPaymentMethod(), normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(paymentMethodRow);

        int numOrderItems = orderInvoiceDetails.getTotalItems();
        String itemsStr = numOrderItems + " item";
        if (numOrderItems > 1) {
            itemsStr += "s";
        }
        View orderItemsNumRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.orderItems),
                itemsStr, normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderItemsNumRow);

        View subTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.subTotal),
                asRupeeSpannable(orderInvoiceDetails.getSubTotal()), normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(subTotalRow);

        if (orderInvoiceDetails.getVatValue() > 0) {
            View vatRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.vat),
                    asRupeeSpannable(orderInvoiceDetails.getVatValue()), normalColor, faceRobotoRegular);
            layoutOrderSummaryInfo.addView(vatRow);
        }

        View deliveryChargeRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                asRupeeSpannable(orderInvoiceDetails.getDeliveryCharge()), normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (orderInvoice.getCreditDetails() != null && orderInvoice.getCreditDetails().size() > 0) {
            for (CreditDetails creditDetails : orderInvoice.getCreditDetails()) {
                View creditDetailRow = UIUtil.getOrderSummaryRow(inflater, creditDetails.getMessage(),
                        asRupeeSpannable(creditDetails.getCreditValue()), normalColor, faceRobotoRegular);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View finalTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                asRupeeSpannable(orderInvoiceDetails.getTotal()), orderTotalLabelColor,
                orderTotalValueColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(finalTotalRow);


        View maxPayableByVoucher = getOrderMaxPayableByVoucher(inflater, getString(R.string.acceptedFoodCoupon),
                orderInvoiceDetails.getFoodValue(), orderTotalLabelColor);
        layoutOrderSummaryInfo.addView(maxPayableByVoucher);

        contentView.removeAllViews();
        contentView.addView(base);

        Button btnShopFromOrder = (Button) base.findViewById(R.id.btnShopFromOrder);
        btnShopFromOrder.setTypeface(faceRobotoRegular);
        btnShopFromOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShopFromThisOrder(orderInvoice.getOrderNumber());
            }
        });


        logOrderDetailSummaryEvent(orderInvoice.getOrderNumber());
    }

    public View getOrderMaxPayableByVoucher(LayoutInflater inflater, String label,
                                            double foodValue, int labelColor) {
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(txtLabel.getTypeface(), Typeface.ITALIC);
        txtLabel.setTextColor(labelColor);
        String foodValueFix = "(" + label + " `";
        String foodValueStr = UIUtil.formatAsMoney(foodValue) + ")";
        int prefixLen = foodValueFix.length();
        SpannableString spannableFoodValue = new SpannableString(foodValueFix + foodValueStr);
        spannableFoodValue.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        txtLabel.setText(spannableFoodValue);
        return row;
    }

    public void renderSlotInfoRow(View row, String slotDate, String slotTime,
                                  String fulfilledBy, boolean hasMultipleSlots) {
        TextView txtNumItems = (TextView) row.findViewById(R.id.txtNumItems);
        TextView txtBasketVal = (TextView) row.findViewById(R.id.txtBasketVal);
        TextView txtSlotDate = (TextView) row.findViewById(R.id.txtSlotDate);
        TextView txtSlotTime = (TextView) row.findViewById(R.id.txtSlotTime);
        TextView txtFulfilledBy = (TextView) row.findViewById(R.id.txtFulfilledBy);
        txtSlotDate.setTypeface(faceRobotoRegular);
        txtSlotTime.setTypeface(faceRobotoRegular);

        txtSlotDate.setText(slotDate);
        txtSlotTime.setText(slotTime);
        txtNumItems.setVisibility(View.GONE);
        txtBasketVal.setVisibility(View.GONE);
        if (!hasMultipleSlots) {
            txtFulfilledBy.setVisibility(View.GONE);
        } else {
            txtFulfilledBy.setTypeface(faceRobotoRegular);
            txtFulfilledBy.setText(fulfilledBy);
        }
    }

    private void logOrderDetailSummaryEvent(String orderNumber) {
        if (getArguments() == null) return;
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ORDER_ID, orderNumber);
        trackEvent(TrackingAware.ORDER_SUMMARY_SHOWN, eventAttribs);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderInvoiceSummaryFragment.class.getName();
    }

    public void onShopFromThisOrder(String orderNumber) {
        Intent intent = new Intent(getActivity(), BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(Constants.ORDER_ID, orderNumber);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_SUMMARY_SCREEN;
    }
}