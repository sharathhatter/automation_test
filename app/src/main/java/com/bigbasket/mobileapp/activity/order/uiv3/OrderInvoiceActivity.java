package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class OrderInvoiceActivity extends BaseActivity implements InvoiceDataAware {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_INVOICE);
        setContentView(R.layout.uiv3_multiple_order_invoice_layout);
        ArrayList<Order> orderArrayList = getIntent().getParcelableArrayListExtra(Constants.ORDERS);
        final String addMoreLink = getIntent().getStringExtra(Constants.ADD_MORE_LINK);
        showOrderList(orderArrayList);

        TextView lblAddMoreProducts = (TextView) findViewById(R.id.lblAddMoreProducts);
        final String addMoreText = lblAddMoreProducts.getText().toString();
        SpannableString spannableString = new SpannableString(addMoreText);
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        lblAddMoreProducts.setText(spannableString);
        lblAddMoreProducts.setTypeface(faceRobotoMedium);
        lblAddMoreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
                intent.putExtra(Constants.WEBVIEW_URL, addMoreLink);
                intent.putExtra(Constants.WEBVIEW_TITLE, addMoreText);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        });
        trackEvent(TrackingAware.THANK_YOU_PAGE_SHOWN, null);
    }


    private void showOrderList(ArrayList<Order> orders) {
        if (orders == null || orders.size() == 0) return;

        TextView txtThankYou = (TextView) findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoBold);

        TextView txtOrderPlaced = (TextView) findViewById(R.id.txtOrderPlaced);
        txtOrderPlaced.setTypeface(faceRobotoMedium, 0);
        txtOrderPlaced.setText(orders.size() > 1 ? getString(R.string.multi_order_place_txt) :
                getString(R.string.order_place_txt));

        LinearLayout layoutOrderNumber = (LinearLayout) findViewById(R.id.layoutOrderNumber);
        //order list
        for (Order order : orders) {
            TextView orderNumberTxtView = new TextView(getCurrentActivity());
            orderNumberTxtView.setTag(order);
            orderNumberTxtView.setTextSize(14);
            orderNumberTxtView.setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
            orderNumberTxtView.setPadding(8, 8, 8, 8);
            SpannableString orderNumSpannable = new SpannableString(getString(R.string.ordernumber) + " " + order.getOrderNumber());
            orderNumSpannable.setSpan(new UnderlineSpan(), 0, orderNumSpannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            orderNumberTxtView.setText(orderNumSpannable);
            orderNumberTxtView.setTypeface(faceRobotoMedium);
            orderNumberTxtView.setGravity(Gravity.CENTER_HORIZONTAL);
            orderNumberTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Order orderTagObj = (Order) view.getTag();
                    if (orderTagObj != null) {
                        showInvoice(orderTagObj);
                    }
                }
            });
            layoutOrderNumber.addView(orderNumberTxtView);
        }
    }

    private void showInvoice(Order order) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getInvoice(order.getOrderId(), new CallbackOrderInvoice<>(this));
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    public void onContinueBtnClicked(View view) {
        goToHome(false);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onBackPressed() {
        goToHome(false);
    }

    @Override
    public void onChangeTitle(String title) {
        goToHome(false);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.THANK_YOU_SCREEN;
    }


}
