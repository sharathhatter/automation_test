package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class OrderThankYouFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Order> orders = getArguments().getParcelableArrayList(Constants.ORDERS);
        showThankyou(orders);
    }

    private void showThankyou(ArrayList<Order> orders) {
        if (orders.size() == 1) {
            renderSingleOrderInvoice(orders.get(0));
        }
    }

    private void renderSingleOrderInvoice(final Order order) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_single_order_invoice_layout, null);
        TextView txtThankYou = (TextView) base.findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoRegular);
        txtThankYou.setText(getString(R.string.orderThankyouText) + order.getOrderNumber());
        TextView txtSlotInfo = (TextView) base.findViewById(R.id.txtSlotInfo);
        txtSlotInfo.setTypeface(faceRobotoRegular);
        txtSlotInfo.setText(order.getDeliveryDate());
        TextView lblViewInvoice = (TextView) base.findViewById(R.id.lblViewInvoice);
        lblViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInvoice(order);
            }
        });
        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void showInvoice(Order order) {
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.GET_INVOICE + "?" + Constants.ORDER_ID + "=" + order.getOrderId();
        startAsyncActivity(url, null, false, false, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.GET_INVOICE)) {
            JsonObject httpResponseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            int status = httpResponseJsonObj.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    JsonObject responseJsonObj = httpResponseJsonObj.get(Constants.RESPONSE).getAsJsonObject();
                    OrderInvoice orderInvoice = ParserUtil.parseOrderInvoice(responseJsonObj);
                    Intent orderDetailIntent = new Intent(getActivity(), OrderDetailActivity.class);
                    orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
                    startActivityForResult(orderDetailIntent, Constants.GO_TO_HOME);
                    break;
                default:
                    // TODO : Implement error handling
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    @Override
    public String getTitle() {
        return "Thank You";
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderThankYouFragment.class.getName();
    }
}
