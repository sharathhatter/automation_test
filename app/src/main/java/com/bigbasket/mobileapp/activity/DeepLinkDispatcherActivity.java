package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.DeepLinkHandler;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class DeepLinkDispatcherActivity extends BaseActivity implements InvoiceDataAware,
        HandlerAware {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchCorrespondingActivity();
    }

    private void launchCorrespondingActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        int resultCode = DeepLinkHandler.handleDeepLink(this, uri);
        if (resultCode == DeepLinkHandler.LOGIN_REQUIRED) {
            showAlertDialog(null, getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, uri);
        } else if (resultCode == DeepLinkHandler.FAILED) {
            showDefaultError();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.DEEP_LINK_DISPATCHER_SCREEN;
    }

    private void showDefaultError() {
        showToast("Page Not Found");
        finish();
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }
}