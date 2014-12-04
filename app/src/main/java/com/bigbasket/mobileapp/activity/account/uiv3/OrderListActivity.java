package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderMonthRange;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class OrderListActivity extends BackButtonActivity implements InvoiceDataAware {

    private Spinner orderDurationSpinner;
    private String orderType;
    private ArrayList<Order> orders;
    private ArrayList<OrderMonthRange> orderMonthRanges;
    private int selectedMonth = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Orders");

        orderType = getIntent().getStringExtra(Constants.ORDER);

        if (savedInstanceState != null) {
            orders = savedInstanceState.getParcelableArrayList(Constants.ORDERS);
            if (orders != null) {
                orderMonthRanges = savedInstanceState.getParcelableArrayList(Constants.ORDER_MONTH_RANGE);
                selectedMonth = savedInstanceState.getInt(Constants.ORDER_RANGE);
            }
            renderOrderList();
            return;
        }
        loadOrders();
    }

    private void loadOrders() {
        loadOrders(-1);
    }

    private void loadOrders(int orderRange) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getOrders(orderType, orderRange > 0 ? String.valueOf(orderRange) : null,
                new Callback<OrderListApiResponse>() {
                    @Override
                    public void success(OrderListApiResponse orderListApiResponse, Response response) {
                        hideProgressDialog();
                        switch (orderListApiResponse.status) {
                            case Constants.OK:
                                orders = orderListApiResponse.orders;
                                orderMonthRanges = orderListApiResponse.orderMonthRanges;
                                selectedMonth = orderListApiResponse.selectedMonth;
                                renderOrderList();
                                break;
                            default:
                                // TODO : Add error handling
                                showAlertDialogFinish(getCurrentActivity(), null, "Server Error");
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        hideProgressDialog();
                        // TODO : Add error handling
                        showAlertDialogFinish(getCurrentActivity(), null, "Server Error");
                    }
                });
    }

    private void renderOrderList() {

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout base = (RelativeLayout) inflater.inflate(R.layout.uiv3_order_list, null);

        AbsListView orderAbsListView = (AbsListView) base.findViewById(R.id.listOrders);
        int spinnerSelectedIdx = OrderMonthRange.getSelectedIndex(orderMonthRanges, selectedMonth);

        if (orderMonthRanges != null && orderMonthRanges.size() > 0) {
            ArrayAdapter<OrderMonthRange> spinnerDateRangeArrayAdapter;
            if (orderDurationSpinner == null) {
                orderDurationSpinner = new Spinner(this);
                spinnerDateRangeArrayAdapter =
                        new ArrayAdapter<OrderMonthRange>(this, android.R.layout.simple_spinner_item, orderMonthRanges) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                if (view instanceof TextView) {
                                    ((TextView) view).setTypeface(faceRobotoRegular);
                                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                    ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                                }
                                return view;
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                if (view instanceof CheckedTextView) {
                                    ((CheckedTextView) view).setTypeface(faceRobotoRegular);
                                    ((CheckedTextView) view).setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
                                } else if (view instanceof TextView) {
                                    ((TextView) view).setTypeface(faceRobotoRegular);
                                    ((TextView) view).setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
                                }
                                return view;
                            }
                        };
                spinnerDateRangeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                orderDurationSpinner.setAdapter(spinnerDateRangeArrayAdapter);
                Toolbar toolbar = getToolbar();
                toolbar.addView(orderDurationSpinner);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            } else {
                spinnerDateRangeArrayAdapter = (ArrayAdapter<OrderMonthRange>) orderDurationSpinner.getAdapter();
            }

            orderDurationSpinner.setSelection(spinnerSelectedIdx);
            orderDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    OrderMonthRange orderMonthRange = orderMonthRanges.get(position);
                    if (orderMonthRange.getValue() != selectedMonth) {
                        loadOrders(orderMonthRange.getValue());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerDateRangeArrayAdapter.notifyDataSetChanged();
        }

        if (orders == null || orders.size() == 0) {
            TextView txtNoOrdersMsg = (TextView) base.findViewById(R.id.txtNoOrdersMsg);
            txtNoOrdersMsg.setTypeface(faceRobotoRegular);
            txtNoOrdersMsg.setVisibility(View.VISIBLE);
            if (orderType.equals(getString(R.string.active_label))) {
                txtNoOrdersMsg.setText(getString(R.string.noActiveOrders));
                base.removeView(orderAbsListView);
            } else if (orderMonthRanges != null) {
                txtNoOrdersMsg.setText(getString(R.string.noOrders) + " " +
                        orderMonthRanges.get(spinnerSelectedIdx).getDisplayValue().toLowerCase());
                base.removeView(orderAbsListView);
            }
        } else {
            OrderListAdapter orderListAdapter = new OrderListAdapter(this, faceRobotoRegular, faceRupee, orders);

            if (orderAbsListView instanceof ListView) {
                ((ListView) orderAbsListView).setAdapter(orderListAdapter);
            } else if (orderAbsListView instanceof GridView) {
                ((GridView) orderAbsListView).setAdapter(orderListAdapter);
            }
            orderAbsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Order order = orders.get(position);
                    showInvoice(order);
                }
            });
        }

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
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
        if (orders != null) {
            outState.putParcelableArrayList(Constants.ORDERS, orders);
            if (orderMonthRanges != null) {
                outState.putParcelableArrayList(Constants.ORDER_MONTH_RANGE, orderMonthRanges);
            }
            outState.putInt(Constants.ORDER_RANGE, selectedMonth);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        startActivityForResult(orderDetailIntent, Constants.GO_TO_HOME);
    }
}