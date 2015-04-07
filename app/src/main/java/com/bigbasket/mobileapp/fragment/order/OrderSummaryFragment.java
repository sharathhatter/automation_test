package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.AbstractOrderSummaryFragment;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;


public class OrderSummaryFragment extends AbstractOrderSummaryFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadPlaceOrderData();
    }

    private void loadPlaceOrderData() {
        OrderSummary orderSummary = getArguments().getParcelable(Constants.ORDER_REVIEW_SUMMARY);
        renderOrderSummary(orderSummary);
    }

    private void renderOrderSummary(OrderSummary orderSummary) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        showProgressView();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_order_review, contentView, false);

        // Set roboto font on static messages
        TextView lblAlmostDone = (TextView) base.findViewById(R.id.lblAlmostDone);
        lblAlmostDone.setTypeface(faceRobotoRegular);
        TextView lblOrderNotYetPlaced = (TextView) base.findViewById(R.id.lblOrderNotYetPlaced);
        lblOrderNotYetPlaced.setTypeface(faceRobotoRegular);


        // Show message if credit card order
        if (orderSummary.getOrderDetails().getPaymentMethod().equals(Constants.PAYU)
                || orderSummary.getOrderDetails().getPaymentMethod().equals(Constants.HDFC_POWER_PAY)) {
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
        renderSlots(layoutDeliverySlot, orderSummary);

        // Show member details
        TextView txtMemberName = (TextView) base.findViewById(R.id.txtMemberName);
        txtMemberName.setTypeface(faceRobotoRegular);
        txtMemberName.setText(orderSummary.getMemberSummary().getMemberName());

        TextView txtMemberAddress = (TextView) base.findViewById(R.id.txtMemberAddress);
        txtMemberAddress.setTypeface(faceRobotoRegular);
        txtMemberAddress.setText(orderSummary.getMemberSummary().getAddress());

        TextView txtMemberContactNum = (TextView) base.findViewById(R.id.txtMemberContactNum);
        if (TextUtils.isEmpty(orderSummary.getMemberSummary().getMobile())) {
            txtMemberContactNum.setVisibility(View.GONE);
        } else {
            txtMemberContactNum.setTypeface(faceRobotoRegular);
            txtMemberContactNum.setText(orderSummary.getMemberSummary().getMobile());
        }

        // Show invoice and other order details
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);
        LinearLayout layoutOrderSummaryInfo = (LinearLayout) base.findViewById(R.id.layoutOrderSummaryInfo);
        OrderDetails orderDetails = orderSummary.getOrderDetails();

        if (orderSummary.getCreditDetails() != null && orderSummary.getCreditDetails().size() > 0) {
            for (CreditDetails creditDetails : orderSummary.getCreditDetails()) {
                View creditDetailRow = getOrderSummaryRow(inflater, creditDetails.getMessage().toUpperCase(),
                        asRupeeSpannable(creditDetails.getCreditValue()), normalColor);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View paymentInformationRow = getOrderSummaryRow(inflater, getString(R.string.paymentMethod).toUpperCase(),
                orderDetails.getPaymentMethodDisplay(), normalColor);
        layoutOrderSummaryInfo.addView(paymentInformationRow);

        String numItems = orderDetails.getTotalItems() + " Item" + (orderDetails.getTotalItems() > 1 ? "s" : "");
        View orderItemsRow = getOrderSummaryRow(inflater, getString(R.string.orderItems).toUpperCase(),
                numItems, normalColor);
        layoutOrderSummaryInfo.addView(orderItemsRow);

        View subTotalRow = getOrderSummaryRow(inflater, getString(R.string.subTotal).toUpperCase(),
                asRupeeSpannable(orderDetails.getSubTotal()), normalColor);
        layoutOrderSummaryInfo.addView(subTotalRow);

        View deliveryChargeRow = getOrderSummaryRow(inflater, getString(R.string.deliveryCharges).toUpperCase(),
                asRupeeSpannable(orderDetails.getDeliveryCharge()), normalColor);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        View finalTotalRow = getOrderSummaryRow(inflater, getString(R.string.finalTotal).toUpperCase(),
                asRupeeSpannable(orderDetails.getFinalTotal()), orderTotalLabelColor, orderTotalValueColor);
        layoutOrderSummaryInfo.addView(finalTotalRow);

        hideProgressView();
        contentView.addView(base);
    }

    private void renderSlots(LinearLayout layoutDeliverySlot, OrderSummary orderSummary) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int numSlots = orderSummary.getSlotGroups().size();
        boolean hasMultipleSlots = numSlots > 1;
        for (int i = 0; i < numSlots; i++) {
            SlotGroup slotGroup = orderSummary.getSlotGroups().get(i);
            View row = inflater.inflate(R.layout.uiv3_slot_info_row, layoutDeliverySlot, false);
            renderSlotInfoRow(row, slotGroup.getSelectedSlot().getFormattedSlotDate(),
                    slotGroup.getSelectedSlot().getDisplayName(),
                    "Slot & fulfilled by " + slotGroup.getFulfillmentInfo().getFulfilledBy(),
                    hasMultipleSlots);
            layoutDeliverySlot.addView(row);
            if (hasMultipleSlots && i != numSlots - 1) {
                View separatorView = new View(getCurrentActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_mini));
                separatorView.setLayoutParams(layoutParams);
                separatorView.setBackgroundColor(getResources().getColor(R.color.uiv3_divider_color));
                layoutDeliverySlot.addView(separatorView);
            }
        }
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void setTitle() {
        // Do nothing
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

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_REVIEW_SUMMARY_SCREEN;
    }
}