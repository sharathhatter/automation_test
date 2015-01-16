package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.adapter.order.OrderListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderMonthRange;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class OrderListActivity extends BackButtonActivity implements InvoiceDataAware {

    private Spinner mOrderDurationSpinner;
    private String mOrderType;
    private ArrayList<Order> mOrders;
    private ArrayList<OrderMonthRange> mOrderMonthRanges;
    private int mSelectedMonth = -1;
    private boolean mIsInShopFromPreviousOrderMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOrderType = getIntent().getStringExtra(Constants.ORDER);
        mIsInShopFromPreviousOrderMode = getIntent().getBooleanExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, false);
        setTitle(mIsInShopFromPreviousOrderMode ? getString(R.string.shopFromPreviousOrder) : getString(R.string.orders));
        if (savedInstanceState != null) {
            mOrders = savedInstanceState.getParcelableArrayList(Constants.ORDERS);
            if (mOrders != null) {
                mOrderMonthRanges = savedInstanceState.getParcelableArrayList(Constants.ORDER_MONTH_RANGE);
                mSelectedMonth = savedInstanceState.getInt(Constants.ORDER_RANGE);
            }
            renderOrderList();
            return;
        }
        loadOrders();
    }

    private void loadOrders() {
        traceOrderEvent(mOrderType, -1);
        loadOrders(-1);
    }

    private void loadOrders(int orderRange) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getOrders(mOrderType, orderRange > 0 ? String.valueOf(orderRange) : null,
                new Callback<OrderListApiResponse>() {
                    @Override
                    public void success(OrderListApiResponse orderListApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (orderListApiResponse.status) {
                            case Constants.OK:
                                mOrders = orderListApiResponse.orders;
                                mOrderMonthRanges = orderListApiResponse.orderMonthRanges;
                                mSelectedMonth = orderListApiResponse.selectedMonth;
                                renderOrderList();
                                break;
                            default:
                                if (orderListApiResponse.errorType.equals(String.valueOf(ApiErrorCodes.INVALID_FIELD))) {
                                    showApiErrorDialog("This is not a valid order number");
                                } else {
                                    handler.sendEmptyMessage(Integer.parseInt(orderListApiResponse.errorType),
                                            orderListApiResponse.message);
                                }
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private void renderOrderList() {

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout base = (RelativeLayout) inflater.inflate(R.layout.uiv3_order_list, contentLayout, false);

        AbsListView orderAbsListView = (AbsListView) base.findViewById(R.id.listOrders);
        int spinnerSelectedIdx = OrderMonthRange.getSelectedIndex(mOrderMonthRanges, mSelectedMonth);

        if (mOrderMonthRanges != null && mOrderMonthRanges.size() > 0) {
            BBArrayAdapter<OrderMonthRange> spinnerDateRangeArrayAdapter;
            if (mOrderDurationSpinner == null) {
                mOrderDurationSpinner = new Spinner(this);
                spinnerDateRangeArrayAdapter =
                        new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, mOrderMonthRanges,
                                faceRobotoRegular, Color.WHITE, getResources().getColor(R.color.uiv3_primary_text_color));
                spinnerDateRangeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mOrderDurationSpinner.setAdapter(spinnerDateRangeArrayAdapter);
                Toolbar toolbar = getToolbar();
                toolbar.addView(mOrderDurationSpinner);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            } else {
                spinnerDateRangeArrayAdapter = (BBArrayAdapter<OrderMonthRange>) mOrderDurationSpinner.getAdapter();
            }

            mOrderDurationSpinner.setSelection(spinnerSelectedIdx);
            mOrderDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    OrderMonthRange orderMonthRange = mOrderMonthRanges.get(position);
                    if (orderMonthRange.getValue() != mSelectedMonth) {
                        traceOrderEvent(mOrderType, orderMonthRange.getValue());
                        loadOrders(orderMonthRange.getValue());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerDateRangeArrayAdapter.notifyDataSetChanged();
        }

        if (mOrders == null || mOrders.size() == 0) {
            TextView txtNoOrdersMsg = (TextView) base.findViewById(R.id.txtNoOrdersMsg);
            txtNoOrdersMsg.setTypeface(faceRobotoRegular);
            txtNoOrdersMsg.setVisibility(View.VISIBLE);
            if (mOrderType.equals(getString(R.string.active_label))) {
                txtNoOrdersMsg.setText(getString(R.string.noActiveOrders));
                base.removeView(orderAbsListView);
            } else if (mOrderMonthRanges != null) {
                txtNoOrdersMsg.setText(getString(R.string.noOrders) + " " +
                        mOrderMonthRanges.get(spinnerSelectedIdx).getDisplayValue().toLowerCase());
                base.removeView(orderAbsListView);
            }
        } else {
            OrderListAdapter orderListAdapter = new OrderListAdapter(this, faceRobotoRegular,
                    faceRupee, mOrders, false, mIsInShopFromPreviousOrderMode);

            if (orderAbsListView instanceof ListView) {
                ((ListView) orderAbsListView).setAdapter(orderListAdapter);
                if (mIsInShopFromPreviousOrderMode) {
                    ((ListView) orderAbsListView).setDivider(new ColorDrawable(getResources().getColor(R.color.uiv3_list_separator_color)));
                    ((ListView) orderAbsListView).setDividerHeight(1);
                }
            } else if (orderAbsListView instanceof GridView) {
                ((GridView) orderAbsListView).setAdapter(orderListAdapter);
            }
            orderAbsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Order order = mOrders.get(position);
                    if (mIsInShopFromPreviousOrderMode) {
                        onShopFromThisOrder(order.getOrderNumber());
                    } else {
                        showInvoice(order);
                    }
                }
            });
        }

        contentLayout.removeAllViews();
        contentLayout.addView(base);
    }

    private void showInvoice(Order order) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getInvoice(order.getOrderId(), new CallbackOrderInvoice<>(this));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mOrders != null) {
            outState.putParcelableArrayList(Constants.ORDERS, mOrders);
            if (mOrderMonthRanges != null) {
                outState.putParcelableArrayList(Constants.ORDER_MONTH_RANGE, mOrderMonthRanges);
            }
            outState.putInt(Constants.ORDER_RANGE, mSelectedMonth);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    private void traceOrderEvent(String orderType, int monthVal) {
        if (orderType.equals(getString(R.string.active_label))) {
            trackEvent(TrackingAware.ORDER_ACTIVE_ORDERS_SHOWN, null);
        } else {
            if (monthVal != -1) {
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.MONTH_RANGE, String.valueOf(monthVal));
                trackEvent(TrackingAware.ORDER_PAST_ORDERS, map);
            } else {
                trackEvent(TrackingAware.ORDER_PAST_ORDERS, null);
            }

        }
    }

    public void onShopFromThisOrder(String orderNumber) {
        Intent intent = new Intent(this, BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(Constants.ORDER_ID, orderNumber);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }
}