package com.bigbasket.mobileapp.fragment.order;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.AbstractOrderSummaryFragment;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderInvoiceDetails;
import com.bigbasket.mobileapp.util.Constants;


public class OrderInvoiceSummaryFragment extends AbstractOrderSummaryFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
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
            renderOrderInvoice(orderInvoice);
        }
    }

    private void renderOrderInvoice(OrderInvoice orderInvoice) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        showProgressView();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_order_invoice_summary, null);

        // Render slots
        LinearLayout layoutDeliverySlot = (LinearLayout) base.findViewById(R.id.layoutDeliverySlot);
        View slotInfoRow = inflater.inflate(R.layout.uiv3_slot_info_row, null);
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

        // Show order & invoice details
        TableLayout layoutOrderSummaryInfo = (TableLayout) base.findViewById(R.id.layoutOrderSummaryInfo);
        OrderInvoiceDetails orderInvoiceDetails = orderInvoice.getOrderInvoiceDetails();

        TableRow orderNumberRow = getOrderSummaryRow(inflater, getString(R.string.ordernumber),
                orderInvoice.getOrderNumber());
        layoutOrderSummaryInfo.addView(orderNumberRow);

        TableRow invoiceNumberRow = getOrderSummaryRow(inflater, getString(R.string.invoicenumber),
                orderInvoice.getInvoiceNumber());
        layoutOrderSummaryInfo.addView(invoiceNumberRow);

        TableRow paymentMethodRow = getOrderSummaryRow(inflater, getString(R.string.paymentMethod),
                orderInvoiceDetails.getPaymentMethod());
        layoutOrderSummaryInfo.addView(paymentMethodRow);

        int numOrderItems = orderInvoiceDetails.getTotalItems();
        String itemsStr = numOrderItems + " item";
        if (numOrderItems > 1) {
            itemsStr += "s";
        }
        TableRow orderItemsNumRow = getOrderSummaryRow(inflater, getString(R.string.orderItems),
                itemsStr);
        layoutOrderSummaryInfo.addView(orderItemsNumRow);

        TableRow deliveryChargeRow = getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                asRupeeSpannable(orderInvoiceDetails.getDeliveryCharge()));
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (orderInvoice.getCreditDetails() != null && orderInvoice.getCreditDetails().size() > 0) {
            for (CreditDetails creditDetails : orderInvoice.getCreditDetails()) {
                TableRow creditDetailRow = getOrderSummaryRow(inflater, creditDetails.getMessage(),
                        asRupeeSpannable(creditDetails.getCreditValue()));
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        if (orderInvoiceDetails.getVatValue() > 0) {
            TableRow vatRow = getOrderSummaryRow(inflater, getString(R.string.vat),
                    asRupeeSpannable(orderInvoiceDetails.getVatValue()));
            layoutOrderSummaryInfo.addView(vatRow);
        }

        TableRow subTotalRow = getOrderSummaryRow(inflater, getString(R.string.subTotal),
                asRupeeSpannable(orderInvoiceDetails.getSubTotal()));
        layoutOrderSummaryInfo.addView(subTotalRow);

        TableRow finalTotalRow = getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                asRupeeSpannable(orderInvoiceDetails.getTotal()));
        layoutOrderSummaryInfo.addView(finalTotalRow);

        contentView.removeAllViews();
        contentView.addView(base);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderInvoiceSummaryFragment.class.getName();
    }
}