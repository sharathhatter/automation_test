package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FlatPageHelper;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;

public class OrderThankyouActivity extends BaseActivity implements InvoiceDataAware {

    private boolean showPayNow = false;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setCurrentScreenName(TrackEventkeys.CO_INVOICE);
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

    private void renderFooter(final ArrayList<Order> orderArrayList) {
        Button btnPayNow = (Button) findViewById(R.id.btnPayNow);
        for (Order order : orderArrayList) {
            if (order.canPay()) {
                showPayNow = true;
                break;
            }
        }
        if (showPayNow) {
            btnPayNow.setVisibility(View.VISIBLE);
        } else {
            btnPayNow.setVisibility(View.GONE);
        }

        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> listSelectedOrderIds = new ArrayList<>();
                Intent intent = new Intent(getCurrentActivity(), PayNowActivity.class);
                for (Order order : orderArrayList) {
                    if (!listSelectedOrderIds.contains(order.getOrderId()))
                        listSelectedOrderIds.add(order.getOrderId());
                }
                intent.putExtra(Constants.ORDER_ID, android.text.TextUtils.join(",", listSelectedOrderIds));
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
                trackEvent(TrackingAware.PAY_NOW_CLICKED, map);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        });
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
                    FlatPageHelper.openFlatPage(getCurrentActivity(), addMoreLink, addMoreMsg);
                }
            });
        } else {
            layoutKnowMore.setVisibility(View.GONE);
        }
    }

    private void showOrderList(ArrayList<Order> orders) {
        if (orders == null || orders.size() == 0) return;

        TextView txtThankYou = (TextView) findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoRegular);

        TextView txtOrderPlaced = (TextView) findViewById(R.id.txtOrderPlaced);
        if (orders.size() > 1) {
            txtOrderPlaced.setTypeface(faceRobotoRegular, 0);
            txtOrderPlaced.setText(R.string.multi_order_place_txt);
        } else {
            txtOrderPlaced.setVisibility(View.GONE);
        }

        renderFooter(orders);

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
                    showInvoice(order);
                }
            });
            TextView txtVariableWeightMsg = (TextView) base.findViewById(R.id.txtVariableWeightMsg);

            if (!TextUtils.isEmpty(order.getVariableWeightMsg()) &&
                    !TextUtils.isEmpty(order.getVariableWeightLink())) {
                txtVariableWeightMsg.setVisibility(View.VISIBLE);
                SpannableString spannableString = new SpannableString(order.getVariableWeightMsg() + " " +
                        getString(R.string.know_more));
                spannableString.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(View view) {
                                                trackEvent(TrackingAware.CHECKOUT_KNOW_MORE_LINK_CLICKED, null);
                                                FlatPageHelper.openFlatPage(getCurrentActivity(), order.getVariableWeightLink(),
                                                        order.getVariableWeightMsg());
                                            }
                                        }, order.getVariableWeightMsg().length() + 1, order.getVariableWeightMsg().length() + 1 +
                                getString(R.string.know_more).length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                txtVariableWeightMsg.setMovementMethod(LinkMovementMethod.getInstance());
                txtVariableWeightMsg.setText(spannableString);
            }
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
                    display += date + " ";
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


    private void showInvoice(Order order) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<OrderInvoice>> call = bigBasketApiService.getInvoice(getPreviousScreenName(), order.getOrderId());
        call.enqueue(new CallbackOrderInvoice<>(this));
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    public void onContinueBtnClicked(View view) {
        goToHome();
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onBackPressed() {
        goToHome();
    }

    @Override
    public void onChangeTitle(String title) {
        goToHome();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.THANK_YOU_SCREEN;
    }
}
