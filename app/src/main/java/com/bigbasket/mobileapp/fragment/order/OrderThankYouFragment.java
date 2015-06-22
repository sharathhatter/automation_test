package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class OrderThankYouFragment extends BaseFragment implements InvoiceDataAware {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Order> orders = getArguments().getParcelableArrayList(Constants.ORDERS);
        //dummy order
        showThankYou(orders);
        trackEvent(TrackingAware.THANK_YOU_PAGE_SHOWN, null);
    }

    private void showThankYou(ArrayList<Order> orders) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup base = (ViewGroup) inflater.inflate(R.layout.uiv3_multiple_order_invoice_layout, contentView, false);

        LinearLayout layoutOrderNumber = (LinearLayout) base.findViewById(R.id.layoutOrderNumber);

        //order list
        TextView orderNumberTxtView = new TextView(getActivity());
        ViewGroup.MarginLayoutParams txtOrderNumberParams = (ViewGroup.MarginLayoutParams) orderNumberTxtView.getLayoutParams();
        txtOrderNumberParams.topMargin = 4;
        orderNumberTxtView.setLayoutParams(txtOrderNumberParams);
        orderNumberTxtView.setText("BBXXXXXXXXXXX");
        orderNumberTxtView.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutOrderNumber.addView(orderNumberTxtView);


        contentView.addView(base);


//        if (orders.size() == 1) {
//            renderSingleOrderInvoice(orders.get(0));
//        } else {
//            renderMultipleOrderInvoice(orders);
//        }
    }

    private void renderMultipleOrderInvoice(final ArrayList<Order> orders) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup base = (ViewGroup) inflater.inflate(R.layout.uiv3_multiple_order_invoice_layout, contentView, false);
//        AbsListView orderAbsListView = (AbsListView) base.findViewById(R.id.listOrders);
//        OrderListAdapter orderListAdapter = new OrderListAdapter(getActivity(), orders, 0);

//        if (orderAbsListView instanceof ListView) {
//            ((ListView) orderAbsListView).setAdapter(orderListAdapter);
//        } else if (orderAbsListView instanceof GridView) {
//            ((GridView) orderAbsListView).setAdapter(orderListAdapter);
//        }
//        orderAbsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Order order = orders.get(position);
//                showInvoice(order);
//            }
//        });

//        TextView lblContinueShopping = (TextView) base.findViewById(R.id.lblContinueShopping);
//        lblContinueShopping.setTypeface(faceRobotoRegular);
//        lblContinueShopping.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getCurrentActivity() != null) {
//                    getCurrentActivity().goToHome(false);
//                }
//            }
//        });

        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void renderSingleOrderInvoice(final Order order) {
        if (getActivity() == null) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_single_order_invoice_layout, contentView, false);
        TextView txtThankYou = (TextView) base.findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoRegular);
        txtThankYou.setText(getString(R.string.orderThankyouText) + order.getOrderNumber());
        TextView txtSlotInfo = (TextView) base.findViewById(R.id.txtSlotInfo);
        txtSlotInfo.setTypeface(faceRobotoRegular);
        txtSlotInfo.setText(order.getDeliveryDate());
        TextView lblViewInvoice = (TextView) base.findViewById(R.id.lblViewInvoice);
        TextView lblContinueShopping = (TextView) base.findViewById(R.id.lblContinueShopping);
        lblViewInvoice.setTypeface(faceRobotoRegular);
        lblContinueShopping.setTypeface(faceRobotoRegular);

        lblViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackEvent(TrackingAware.THANK_YOU_VIEW_INVOICE_CLICKED, null);
                showInvoice(order);
            }
        });
        lblContinueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentActivity() != null) {
                    getCurrentActivity().goToHome(false);
                }
            }
        });

        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void showInvoice(Order order) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        bigBasketApiService.getInvoice(order.getOrderId(), new CallbackOrderInvoice<>(this));
    }

    @Override
    public String getTitle() {
        return getString(R.string.thankYouHeader);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderThankYouFragment.class.getName();
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_THANK_YOU_PAGE);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.THANK_YOU_SCREEN;
    }
}
