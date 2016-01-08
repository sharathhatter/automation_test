package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import retrofit.Call;

public class PayNowThankyouActivity extends BaseActivity implements InvoiceDataAware {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_paynow_thankyou);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_PAY_NOW_THANK_YOU);
        final String orderId = getIntent().getStringExtra(Constants.ORDER_ID);

        TextView txtThankYou = (TextView) findViewById(R.id.txtThankYou);
        txtThankYou.setTypeface(faceRobotoBold);

        TextView txtOrderPlaced = (TextView) findViewById(R.id.txtOrderPlaced);
        txtOrderPlaced.setTypeface(faceRobotoMedium, 0);

        TextView txtOrderId = (TextView) findViewById(R.id.txtOrderId);
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
