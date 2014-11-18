package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.AbstractOrderSummaryFragment;
import com.bigbasket.mobileapp.interfaces.PlaceOrderAware;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;


public class OrderSummaryFragment extends AbstractOrderSummaryFragment {

    private OrderSummary orderSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadPlaceOrderData();
    }

    private void loadPlaceOrderData() {
        orderSummary = getArguments().getParcelable(Constants.ACTION_TAB_TAG);
        renderOrderSummary();
    }

    private void renderOrderSummary() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        showProgressView();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_order_review, null);

        // Set roboto font on static messages
        TextView lblAlmostDone = (TextView) base.findViewById(R.id.lblAlmostDone);
        lblAlmostDone.setTypeface(faceRobotoRegular);
        TextView lblOrderNotYetPlaced = (TextView) base.findViewById(R.id.lblOrderNotYetPlaced);
        lblOrderNotYetPlaced.setTypeface(faceRobotoRegular);


        // Show message if credit card order
        if (orderSummary.getOrderDetails().getPaymentMethod().equals(Constants.CREDIT_CARD)) {
            TextView txtOrderSummaryMsg = (TextView) base.findViewById(R.id.txtOrderSummaryMsg);
            txtOrderSummaryMsg.setTypeface(faceRobotoRegular);
            String orderSummaryDefaultMsg = txtOrderSummaryMsg.getText().toString();
            String paymentGatewayRedirectMsg = "\n\n" + getString(R.string.paymentGatewayRedirectMsg);
            Spannable spannable = new SpannableString(orderSummaryDefaultMsg + paymentGatewayRedirectMsg);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), orderSummaryDefaultMsg.length(),
                    spannable.length() - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dark_red)),
                    orderSummaryDefaultMsg.length(),
                    spannable.length() - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            txtOrderSummaryMsg.setText(spannable);
        }

        // Show delivery slots
        LinearLayout layoutDeliverySlot = (LinearLayout) base.findViewById(R.id.layoutDeliverySlot);
        renderSlots(layoutDeliverySlot);

        // Show member details
        TextView txtMemberName = (TextView) base.findViewById(R.id.txtMemberName);
        txtMemberName.setTypeface(faceRobotoRegular);
        txtMemberName.setText(orderSummary.getMemberSummary().getMemberName());

        TextView txtMemberAddress = (TextView) base.findViewById(R.id.txtMemberAddress);
        txtMemberAddress.setTypeface(faceRobotoRegular);
        txtMemberAddress.setText(orderSummary.getMemberSummary().getAddress());

        // Show invoice and other order details
        TableLayout layoutOrderSummaryInfo = (TableLayout) base.findViewById(R.id.layoutOrderSummaryInfo);
        OrderDetails orderDetails = orderSummary.getOrderDetails();

        if (orderSummary.getCreditDetails() != null && orderSummary.getCreditDetails().size() > 0) {
            for (CreditDetails creditDetails : orderSummary.getCreditDetails()) {
                TableRow creditDetailRow = getOrderSummaryRow(inflater, creditDetails.getMessage(),
                        asRupeeSpannable(creditDetails.getCreditValue()));
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        TableRow paymentInformationRow = getOrderSummaryRow(inflater, getString(R.string.paymentMethod),
                orderDetails.getPaymentMethodDisplay());
        layoutOrderSummaryInfo.addView(paymentInformationRow);

        String numItems = orderDetails.getTotalItems() + " Item" + (orderDetails.getTotalItems() > 1 ? "s" : "");
        TableRow orderItemsRow = getOrderSummaryRow(inflater, getString(R.string.orderItems),
                numItems);
        layoutOrderSummaryInfo.addView(orderItemsRow);

        TableRow subTotalRow = getOrderSummaryRow(inflater, getString(R.string.subTotal),
                asRupeeSpannable(orderDetails.getSubTotal()));
        layoutOrderSummaryInfo.addView(subTotalRow);

        TableRow deliveryChargeRow = getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                asRupeeSpannable(orderDetails.getDeliveryCharge()));
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        TableRow finalTotalRow = getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                asRupeeSpannable(orderDetails.getFinalTotal()));
        layoutOrderSummaryInfo.addView(finalTotalRow);

        TextView txtPlaceOrder = (TextView) base.findViewById(R.id.txtPlaceOrder);
        txtPlaceOrder.setTypeface(faceRobotoThin);
        txtPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PlaceOrderAware) getActivity()).onPlaceOrderAction(orderSummary);
            }
        });

        hideProgressView();
        contentView.addView(base);
    }

    private void renderSlots(LinearLayout layoutDeliverySlot) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        boolean hasMultipleSlots = orderSummary.getSlotGroups().size() > 1;
        for (SlotGroup slotGroup : orderSummary.getSlotGroups()) {
            View row = inflater.inflate(R.layout.uiv3_slot_info_row, null);
            renderSlotInfoRow(row, slotGroup.getSelectedSlot().getFormattedSlotDate(),
                    slotGroup.getSelectedSlot().getDisplayName(),
                    "Slot & fulfilled by " + slotGroup.getFulfillmentInfo().getFulfilledBy(),
                    hasMultipleSlots);
            layoutDeliverySlot.addView(row);
        }
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderSummaryFragment.class.getName();
    }
}