package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractOrderSummaryFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderInvoiceDetails;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;
import java.util.Map;


public class OrderInvoiceSummaryFragment extends AbstractOrderSummaryFragment {

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

        View orderNumberRow = getOrderSummaryRow(inflater, getString(R.string.ordernumber),
                orderInvoice.getOrderNumber(), normalColor);
        layoutOrderSummaryInfo.addView(orderNumberRow);

        View invoiceNumberRow = getOrderSummaryRow(inflater, getString(R.string.invoicenumber),
                orderInvoice.getInvoiceNumber(), normalColor);
        layoutOrderSummaryInfo.addView(invoiceNumberRow);

        View paymentMethodRow = getOrderSummaryRow(inflater, getString(R.string.paymentMethod),
                orderInvoiceDetails.getPaymentMethod(), normalColor);
        layoutOrderSummaryInfo.addView(paymentMethodRow);

        int numOrderItems = orderInvoiceDetails.getTotalItems();
        String itemsStr = numOrderItems + " item";
        if (numOrderItems > 1) {
            itemsStr += "s";
        }
        View orderItemsNumRow = getOrderSummaryRow(inflater, getString(R.string.orderItems),
                itemsStr, normalColor);
        layoutOrderSummaryInfo.addView(orderItemsNumRow);

        View subTotalRow = getOrderSummaryRow(inflater, getString(R.string.subTotal),
                asRupeeSpannable(orderInvoiceDetails.getSubTotal()), normalColor);
        layoutOrderSummaryInfo.addView(subTotalRow);

        if (orderInvoiceDetails.getVatValue() > 0) {
            View vatRow = getOrderSummaryRow(inflater, getString(R.string.vat),
                    asRupeeSpannable(orderInvoiceDetails.getVatValue()), normalColor);
            layoutOrderSummaryInfo.addView(vatRow);
        }

        View deliveryChargeRow = getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                asRupeeSpannable(orderInvoiceDetails.getDeliveryCharge()), normalColor);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (orderInvoice.getCreditDetails() != null && orderInvoice.getCreditDetails().size() > 0) {
            for (CreditDetails creditDetails : orderInvoice.getCreditDetails()) {
                View creditDetailRow = getOrderSummaryRow(inflater, creditDetails.getMessage(),
                        asRupeeSpannable(creditDetails.getCreditValue()), normalColor);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View finalTotalRow = getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                asRupeeSpannable(orderInvoiceDetails.getTotal()), orderTotalLabelColor,
                orderTotalValueColor);
        layoutOrderSummaryInfo.addView(finalTotalRow);

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

    private void logOrderDetailSummaryEvent(String orderNumber) {
        if (getArguments() == null) return;
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ORDER_ID, orderNumber);
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getArguments().getString(TrackEventkeys.NAVIGATION_CTX));
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
        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(TrackEventkeys.NAVIGATION_CTX, getArguments().getString(TrackEventkeys.NAVIGATION_CTX));
        intent.putExtra(Constants.ORDER_ID, orderNumber);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_SUMMARY_SCREEN;
    }
}