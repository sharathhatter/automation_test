package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import retrofit2.Call;
import java.util.ArrayList;

public class PayNowThankyouActivity extends BaseActivity implements InvoiceDataAware {

    private String orderId;
    private ArrayList<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_paynow_thankyou);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_PAY_NOW_THANK_YOU);
        if (getIntent().hasExtra(Constants.ORDER_ID)) {
            orderId = getIntent().getStringExtra(Constants.ORDER_ID);
        }
        boolean isFromPayNow = false;
        if (getIntent().hasExtra(Constants.ORDERS)) {
            isFromPayNow = getIntent().getBooleanExtra(Constants.IS_FROM_PAYNOW, false);
            orders = getIntent().getParcelableArrayListExtra(Constants.ORDERS);
        }

        TextView txtOrderId = (TextView) findViewById(R.id.txtOrderId);
        TextView txtThankYou = (TextView) findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoBold);

        TextView txtOrderPlaced = (TextView) findViewById(R.id.txtOrderPlaced);
        txtOrderPlaced.setTypeface(faceRobotoMedium, 0);
        if (!isFromPayNow) {
            txtOrderId.setVisibility(View.VISIBLE);
            txtOrderId.setTypeface(faceRobotoMedium);
            SpannableString orderNumSpannable = new SpannableString(getString(R.string.ordernumber) + " " + orderId);
            orderNumSpannable.setSpan(new UnderlineSpan(), 0, orderNumSpannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtOrderId.setText(orderNumSpannable);
            txtOrderId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInvoice(orderId);
                }
            });
        } else {
            txtOrderId.setVisibility(View.GONE);
            renderOrders(orders);
        }
    }

    private void renderOrders(ArrayList<Order> orders) {

        LinearLayout layoutOrderNumber = (LinearLayout) findViewById(R.id.layoutOrderNumber);
        LayoutInflater inflater = getLayoutInflater();
        //order list
        for (final Order order : orders) {
            View base = inflater.inflate(R.layout.uiv3_single_pay_now_row, layoutOrderNumber, false);
            TextView txtOrderNum = (TextView) base.findViewById(R.id.txtOrderNum);

            TextView txtAmount = (TextView) base.findViewById(R.id.txtAmount);
            txtAmount.setTypeface(faceRobotoRegular);
            String orderPrefix = " `";
            String orderValStr = UIUtil.formatAsMoney(Double.parseDouble(order.getOrderValue()));
            int prefixLen = orderPrefix.length();
            SpannableString spannableMrp = new SpannableString(orderPrefix + orderValStr);
            spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtAmount.append(spannableMrp);

            String prefix = getString(R.string.ordernumberWithSpace);
            SpannableString orderNumSpannable = new SpannableString(prefix + order.getOrderNumber());
            orderNumSpannable.setSpan(new UnderlineSpan(), prefix.length(), orderNumSpannable.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtOrderNum.setText(orderNumSpannable);
            txtOrderNum.setTypeface(faceRobotoRegular);
            txtOrderNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInvoice(order.getOrderId());
                }
            });

            CheckBox check = (CheckBox) base.findViewById(R.id.mOrderPos);
            check.setVisibility(View.GONE);

            TextView numOfItems = (TextView) base.findViewById(R.id.txtNumOfItems);
            if (order.getItemsCount() > 0) {
                String itemString = order.getItemsCount() > 1 ? " Items" : " Item";
                numOfItems.setText(order.getItemsCount() + itemString);
            }

            TextView txtSlotTime = (TextView) base.findViewById(R.id.txtStndDel);
            txtSlotTime.setTextColor(ContextCompat.getColor(this, R.color.uiv3_thank_tou_red));
            if (order.getSlotDisplay() != null) {
                String date = order.getSlotDisplay().getDate();
                String time = order.getSlotDisplay().getTime();
                String display = "";
                if (!TextUtils.isEmpty(date)) {
                    display += date + "\n";
                }
                if (!TextUtils.isEmpty(time)) {
                    if (!TextUtils.isEmpty(display)) {
                        display += " ";
                    }
                    display += time;
                }
                txtSlotTime.setText(getString(R.string.delivery_time_with_space) + display);
                txtSlotTime.setTypeface(faceRobotoRegular);
            } else {
                txtSlotTime.setVisibility(View.GONE);
            }
            layoutOrderNumber.addView(base);
        }
    }

    private void showInvoice(String orderId) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<OrderInvoice>> call = bigBasketApiService.getInvoice(getPreviousScreenName(), orderId);
        call.enqueue(new CallbackOrderInvoice<>(this));
    }

    public void onContinueBtnClicked(View v) {
        goToHome();
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public void onBackPressed() {
        goToHome();
    }

    @Override
    public String getScreenTag() {
        return PayNowThankyouActivity.class.getName();
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }
}
