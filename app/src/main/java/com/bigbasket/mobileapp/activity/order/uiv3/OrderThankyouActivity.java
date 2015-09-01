package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class OrderThankyouActivity extends BaseActivity implements InvoiceDataAware {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_INVOICE);
        setContentView(R.layout.uiv3_multiple_order_invoice_layout);

        ImageView imgBBLogo = (ImageView) findViewById(R.id.imgBBLogo);
        UIUtil.displayAsyncImage(imgBBLogo, R.drawable.bb_logo_transparent_bkg);

        ArrayList<Order> orderArrayList = getIntent().getParcelableArrayListExtra(Constants.ORDERS);
        String addMoreLink = getIntent().getStringExtra(Constants.ADD_MORE_LINK);
        String addMoreMsg = getIntent().getStringExtra(Constants.ADD_MORE_MSG);
        showOrderList(orderArrayList);
        showAddMoreText(addMoreLink, addMoreMsg);

        trackEvent(TrackingAware.THANK_YOU_PAGE_SHOWN, null);
    }

    private void showAddMoreText(final String addMoreLink, final String addMoreMsg) {
        ViewGroup layoutKnowMore = (ViewGroup) findViewById(R.id.layoutKnowMore);
        if (!TextUtils.isEmpty(addMoreMsg) && !TextUtils.isEmpty(addMoreLink)) {
            TextView txtAddMoreProducts = (TextView) findViewById(R.id.txtAddMoreProducts);
            txtAddMoreProducts.setText(addMoreMsg);
            txtAddMoreProducts.setTypeface(faceRobotoRegular);

            TextView lblKnowMore = (TextView) findViewById(R.id.lblKnowMore);
            SpannableString spannableString = new SpannableString(lblKnowMore.getText());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            lblKnowMore.setText(spannableString);
            lblKnowMore.setTypeface(faceRobotoBold);
            lblKnowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trackEvent(TrackingAware.CHECKOUT_KNOW_MORE_LINK_CLICKED, null);
                    Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                    intent.putExtra(Constants.WEBVIEW_URL, addMoreLink);
                    intent.putExtra(Constants.WEBVIEW_TITLE, addMoreMsg);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        } else {
            layoutKnowMore.setVisibility(View.GONE);
        }
    }

    private void showOrderList(ArrayList<Order> orders) {
        if (orders == null || orders.size() == 0) return;

        TextView txtThankYou = (TextView) findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoBold);

        TextView txtOrderPlaced = (TextView) findViewById(R.id.txtOrderPlaced);
        if (orders.size() > 1) {
            txtOrderPlaced.setTypeface(faceRobotoRegular, 0);
            txtOrderPlaced.setText(R.string.multi_order_place_txt);
        } else {
            txtOrderPlaced.setVisibility(View.GONE);
        }

        LinearLayout layoutOrderNumber = (LinearLayout) findViewById(R.id.layoutOrderNumber);
        LayoutInflater inflater = getLayoutInflater();
        //order list
        for (final Order order : orders) {
            View base = inflater.inflate(R.layout.uiv3_order_thankyou_row, layoutOrderNumber, false);
            TextView txtOrderNum = (TextView) base.findViewById(R.id.txtOrderNum);
            SpannableString orderNumSpannable = new SpannableString(getString(R.string.ordernumber) + " " + order.getOrderNumber());
            orderNumSpannable.setSpan(new UnderlineSpan(), 0, orderNumSpannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtOrderNum.setText(orderNumSpannable);
            txtOrderNum.setTypeface(faceRobotoRegular);
            txtOrderNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInvoice(order);
                }
            });

            TextView txtSlotTime = (TextView) base.findViewById(R.id.txtSlotTime);
            if (order.getSlotDisplay() != null) {
                String date = order.getSlotDisplay().getDate();
                String time = order.getSlotDisplay().getTime();
                String display = "";
                if (!TextUtils.isEmpty(date)) {
                    display += date;
                }
                if (!TextUtils.isEmpty(time)) {
                    if (!TextUtils.isEmpty(display)) {
                        display += " ";
                    }
                    display += time;
                }
                txtSlotTime.setText(getString(R.string.delivery_time) + " " + display);
                txtSlotTime.setTypeface(faceRobotoRegular);
            } else {
                txtSlotTime.setVisibility(View.GONE);
            }

            Button btnPayNow = (Button) base.findViewById(R.id.btnPayNow);
            if (order.canPay()) {
                btnPayNow.setTypeface(faceRobotoBold);
                btnPayNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getCurrentActivity(), PayNowActivity.class);
                        intent.putExtra(Constants.ORDER_ID, order.getOrderId());
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                });
            } else {
                btnPayNow.setVisibility(View.GONE);
            }
            layoutOrderNumber.addView(base);
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
