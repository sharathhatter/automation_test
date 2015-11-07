package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.adapter.order.OrderListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.GetMoreOrderAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.OrderItemClickAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;


public class OrderListActivity extends BackButtonActivity implements InvoiceDataAware,
        GetMoreOrderAware, OrderItemClickAware {

    private String mOrderType;
    private boolean mIsInShopFromPreviousOrderMode;
    private int currentPage = 1;
    private int totalPages;
    private OrderListAdapter orderListAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.ORDER_HISTORY);
        mOrderType = getIntent().getStringExtra(Constants.ORDER);
        mIsInShopFromPreviousOrderMode = getIntent().getBooleanExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, false);
        setTitle(mIsInShopFromPreviousOrderMode ? getString(R.string.shopFromPreviousOrder) :
                getString(R.string.my_order_label));
        loadOrders(currentPage);

    }

    @Override
    public void getMoreOrders() {
        loadOrders(++currentPage);
    }

    private void loadOrders(int page) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        if (page == 1)
            showProgressView();
        Call<ApiResponse<OrderListApiResponse>> call = bigBasketApiService.getOrders(mOrderType, String.valueOf(page));
        call.enqueue(new BBNetworkCallback<ApiResponse<OrderListApiResponse>>(this) {
            @Override
            public void onSuccess(ApiResponse<OrderListApiResponse> orderListApiResponse) {
                if (orderListApiResponse.status == 0) {
                    totalPages = orderListApiResponse.apiResponseContent.totalPages;
                    renderOrderList(orderListApiResponse.apiResponseContent.orders);
                } else {
                    handler.sendEmptyMessage(orderListApiResponse.status,
                            orderListApiResponse.message, true);
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressView();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    private void showEmptyPage(LayoutInflater inflater, FrameLayout contentLayout) {
        View emptyPageView = inflater.inflate(R.layout.uiv3_empty_data_text, contentLayout, false);
        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);

        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(false);
            }
        });

        imgEmptyPage.setImageResource(R.drawable.empty_order_history);
        txtEmptyMsg1.setText(getString(R.string.noOrdersPlaced));
        contentLayout.removeAllViews();
        contentLayout.addView(emptyPageView);
    }

    @SuppressWarnings("unchecked")
    private void renderOrderList(final ArrayList<Order> mOrders) {

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout base = (RelativeLayout) inflater.inflate(R.layout.uiv3_order_list, contentLayout, false);

        RecyclerView orderListView = (RecyclerView) base.findViewById(R.id.listOrders);

        UIUtil.configureRecyclerView(orderListView, this, 1, 1);
        if (currentPage == 1 && (mOrders == null || mOrders.size() == 0)) {
            showEmptyPage(inflater, contentLayout);

        } else {
            if (orderListAdapter == null) {
                orderListAdapter = new OrderListAdapter<>(this, mOrders, totalPages);
                orderListAdapter.setCurrentPage(currentPage);
            }

            ArrayList<Order> olderOrderList = orderListAdapter.getOrders();
            if (currentPage > 1 && olderOrderList != null && olderOrderList.size() > 0
                    && mOrders != null && mOrders.size() > 0) {
                updateOrderList(olderOrderList, mOrders);
            } else {
                orderListView.setAdapter(orderListAdapter);
                contentLayout.removeAllViews();
                contentLayout.addView(base);
            }
        }

        trackEvent(TrackingAware.MY_ORDER_SHOWN, null);
    }

    @Override
    public void onOrderItemClicked(Order order) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        trackEvent(TrackingAware.MY_ORDER_ITEM_CLICKED, map);
        if (mIsInShopFromPreviousOrderMode) {
            onShopFromThisOrder(order.getOrderNumber());
        } else {
            showInvoice(order);
        }
    }


    private void updateOrderList(ArrayList<Order> olderOrderList, ArrayList<Order> newOrderList) {
        int insertedAt = olderOrderList.size();
        olderOrderList.addAll(newOrderList);
        orderListAdapter.setCurrentPage(currentPage);
        orderListAdapter.notifyItemRangeInserted(insertedAt, newOrderList.size());
    }

    private void showInvoice(Order order) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<OrderInvoice>> call = bigBasketApiService.getInvoice(order.getOrderId());
        call.enqueue(new CallbackOrderInvoice<>(this));
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    private void onShopFromThisOrder(String orderNumber) {
        Intent intent = new Intent(this, BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(Constants.ORDER_ID, orderNumber);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_MEMBER_ORDER_SCREEN;
    }
}