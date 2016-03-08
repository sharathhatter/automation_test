package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.adapter.order.OrderListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.GetMoreOrderAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.OrderItemClickAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnOrderSelectionChanged;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import retrofit2.Call;

public class OrderListActivity extends BackButtonActivity implements InvoiceDataAware,
        GetMoreOrderAware, OrderItemClickAware, OnOrderSelectionChanged {

    private static final String SELECTED_ORDER_IDS = "selected_list";
    private ViewGroup layoutCheckoutFooter;
    private String mOrderType;
    private boolean mIsInShopFromPreviousOrderMode;
    private OrderListAdapter orderListAdapter = null;
    private int currentPage = 1;
    private Collection listSelectedOrders;
    private CoordinatorLayout base;
    private LayoutInflater inflater;
    private FrameLayout contentLayout;
    private RecyclerView orderListView;
    private TextView txtTotal;
    private TextView txtAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.ORDER_HISTORY);
        mOrderType = getIntent().getStringExtra(Constants.ORDER);
        mIsInShopFromPreviousOrderMode = getIntent().getBooleanExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, false);
        setTitle(mIsInShopFromPreviousOrderMode ? getString(R.string.shopFromPreviousOrder) :
                getString(R.string.my_order_label));
        registerViews(savedInstanceState);
    }

    private void registerViews(Bundle savedInstanceState) {
        contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = (CoordinatorLayout) inflater.inflate(R.layout.uiv3_order_list, contentLayout, false);
        orderListView = (RecyclerView) base.findViewById(R.id.listOrders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        orderListView.setLayoutManager(linearLayoutManager);
        layoutCheckoutFooter = (ViewGroup) base.findViewById(R.id.layoutCheckoutFooter);
        txtTotal = (TextView) layoutCheckoutFooter.findViewById(R.id.txtTotal);
        txtAction = (TextView) layoutCheckoutFooter.findViewById(R.id.txtAction);
        renderFooter();
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(Constants.CURRENT_PAGE, 1);
            if (savedInstanceState.containsKey(Constants.ORDERS)) {
                ArrayList<Order> orders = savedInstanceState.getParcelableArrayList(Constants.ORDERS);
                int totalPages = savedInstanceState.getInt(Constants.TOTAL);
                initAdapter(orders, totalPages, currentPage);
                if (savedInstanceState.containsKey(SELECTED_ORDER_IDS)) {
                    orderListAdapter.setSelectedList(
                            savedInstanceState.getStringArrayList(SELECTED_ORDER_IDS));
                }
            } else {
                currentPage = 1;
                loadOrders(currentPage);
            }
        } else {
            loadOrders(currentPage);
        }
    }

    private void renderFooter() {
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.payNow), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (orderListAdapter != null) {
                    listSelectedOrders = orderListAdapter.getSelectedItems();
                    Collection<String> ids = orderListAdapter.getSelectedOrderIds();
                    startPayNow(new ArrayList(ids));
                }
            }
        });
    }

    private void startPayNow(ArrayList orderIds) {
        if (orderIds == null || orderIds.size() == 0) return;
        if (!checkInternetConnection()) {
            handler.sendOfflineError(false);
            return;
        }
        Intent intent = new Intent(OrderListActivity.this, PayNowActivity.class);
        intent.putExtra(Constants.ORDER_ID, android.text.TextUtils.join(",", orderIds));
        intent.putParcelableArrayListExtra(Constants.ORDER,
                new ArrayList<Order>(listSelectedOrders));
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.PAY_NOW_CLICKED, map);
        startActivityForResult(intent, NavigationCodes.RC_PAY_NOW);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mIsInShopFromPreviousOrderMode) {
            if (orderListAdapter != null) {
                if (orderListAdapter.isInSelectionMode()) {
                    layoutCheckoutFooter.setVisibility(View.GONE);
                    onOrderSelectionChanged(0, 0);//resetting the action-bar
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CURRENT_PAGE, currentPage);
        if (orderListAdapter != null && orderListAdapter.getOrders() != null) {
            outState.putInt(Constants.TOTAL, orderListAdapter.getTotalPages());
            outState.putParcelableArrayList(Constants.ORDERS, orderListAdapter.getOrders());
            if (orderListAdapter.isInSelectionMode()) {
                outState.putStringArrayList(SELECTED_ORDER_IDS,
                        new ArrayList<String>(orderListAdapter.getSelectedOrderIds()));
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void getMoreOrders(int nextPage) {
        loadOrders(nextPage);
    }

    private void setPrice(String total) {
        if (!TextUtils.isEmpty(total)) {
            String totalLabel = getString(R.string.totalMrp) + " ";
            String rupeeSym = "`";
            SpannableString spannableString = new SpannableString(totalLabel + rupeeSym +
                    total);
            spannableString.setSpan(new CustomTypefaceSpan("", FontHolder.getInstance(this).getFaceRupee()),
                    totalLabel.length(),
                    totalLabel.length() + rupeeSym.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtTotal.setVisibility(View.VISIBLE);
            txtTotal.setText(spannableString);
        } else {
            txtTotal.setVisibility(View.GONE);
        }
    }

    private void loadOrders(final int page) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        if (page == 1)
            showProgressView();
        Call<ApiResponse<OrderListApiResponse>> call =
                bigBasketApiService.getOrders(getPreviousScreenName(), mOrderType, String.valueOf(page));
        call.enqueue(new BBNetworkCallback<ApiResponse<OrderListApiResponse>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<OrderListApiResponse> orderListApiResponse) {
                if (orderListApiResponse.status == 0) {
                    renderOrderList(orderListApiResponse.apiResponseContent.orders, page,
                            orderListApiResponse.apiResponseContent.totalPages);
                    currentPage = page;
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

    private void showSnackBar() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAlreadySeenTheMsg = preferences.getBoolean(Constants.HAS_USER_SEEN_PAYMENT_MSG, false);
        if (!isAlreadySeenTheMsg) {
            showToast(getString(R.string.pay_for_multiple_orders));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.HAS_USER_SEEN_PAYMENT_MSG, true);
            editor.apply();
        }
    }

    private void showEmptyPage() {
        View emptyPageView = inflater.inflate(R.layout.uiv3_empty_data_text, contentLayout, false);
        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);

        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });

        imgEmptyPage.setImageResource(R.drawable.empty_order_history);
        txtEmptyMsg1.setText(getString(R.string.noOrdersPlaced));
        contentLayout.removeAllViews();
        contentLayout.addView(emptyPageView);
    }

    private void initAdapter(ArrayList<Order> orders, int totalPages, int currentPage) {
        orderListAdapter = new OrderListAdapter<>(this, orders, totalPages, currentPage);
        orderListView.setAdapter(orderListAdapter);
        contentLayout.removeAllViews();
        contentLayout.addView(base);
    }

    @SuppressWarnings("unchecked")
    private void renderOrderList(final ArrayList<Order> orders, int currentPage, int totalPages) {
        if (currentPage == 1 && (orders == null || orders.size() == 0)) {
            showEmptyPage();
        } else {
            if (orderListAdapter == null) {
                initAdapter(orders, totalPages, currentPage);
            } else {
                if (currentPage == 1) {
                    orderListAdapter.clear();
                    orderListAdapter.updateOrderList(orders, currentPage, totalPages);
                } else if (orders != null && orders.size() > 0) {
                    orderListAdapter.updateOrderList(orders, currentPage, totalPages);
                }
            }
        }
        trackEvent(TrackingAware.MY_ORDER_SHOWN, null);
    }

    @Override
    public void onOrderItemClicked(Order order) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.MY_ORDER_ITEM_CLICKED, map);
        if (mIsInShopFromPreviousOrderMode) {
            onShopFromThisOrder(order.getOrderNumber());
        } else {
            showInvoice(order);
        }
    }

    private void showInvoice(Order order) {
        if (order != null) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<OrderInvoice>> call = bigBasketApiService.getInvoice(getCurrentScreenName(), order.getOrderId());
            call.enqueue(new CallbackOrderInvoice<>(this));
        }
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    private void onShopFromThisOrder(String orderNumber) {
        onOrderSelectionChanged(0, 0);//resetting the action-bar before navigating to next activity
        Intent intent = new Intent(this, BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(Constants.ORDER_ID, orderNumber);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NavigationCodes.RC_PAY_NOW) {
            onOrderSelectionChanged(0, 0);
            if (resultCode == NavigationCodes.REFRESH_ORDERS) {
                if (orderListAdapter != null) {
                    orderListAdapter.clear();
                }
                loadOrders(1);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_MEMBER_ORDER_SCREEN;
    }

    @Override
    public void onOrderSelectionChanged(int count, double total) {
        if (count == 0) {
            setTitleandSubTitle(getString(R.string.my_orders), null);
            layoutCheckoutFooter.setVisibility(View.GONE);
            if (orderListAdapter != null) {
                orderListAdapter.clearSelection();
            }
        } else {
            showSnackBar();
            layoutCheckoutFooter.setVisibility(View.VISIBLE);
            setPrice("" + total);
            setTitleandSubTitle(getString(R.string.payNow), getString(R.string.selected_count, count));
            txtAction.setText(getString(R.string.continueCaps));
        }
    }
}