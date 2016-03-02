package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.DeepLinkHandler;
import com.bigbasket.mobileapp.handler.SilentDeepLinkHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.moe.pushlibrary.utils.MoEHelperConstants;

public class DeepLinkDispatcherActivity extends BaseActivity implements InvoiceDataAware,
        AppOperationAware {

    protected BigBasketMessageHandler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new SilentDeepLinkHandler<>(this);
        if(savedInstanceState == null) {
            launchCorrespondingActivity();
        }
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    private void launchCorrespondingActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            goToHome();
            return;
        }

        // Deep link can be from dynamic screen also, hence we need to preserve that referer
        if (getPreviousScreenName() == null)
            setCurrentScreenName(TrackEventkeys.DEEP_LINK);
        else
            setCurrentScreenName(getPreviousScreenName());

        int resultCode = DeepLinkHandler.handleDeepLink(this, uri);
        if (resultCode == DeepLinkHandler.LOGIN_REQUIRED) {
            showToast(getString(R.string.login_required));
            Bundle data = new Bundle(1);
            data.putString(Constants.DEEPLINK_URL, uri.toString());
            launchLogin(TrackEventkeys.NAVIGATION_CTX_DIALOG, data, true);
            finish();
        } else if (resultCode == DeepLinkHandler.REGISTER_DEVICE_REQUIRED) {
            /**
             * launch splash activity for visitor registration
             */
            launchSplashActivity();

        } else if (resultCode == DeepLinkHandler.FAILED) {
            showDefaultError();
        }
    }

    private void handleBackStack() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        String sourceName = uri.getQueryParameter(MoEHelperConstants.NAVIGATION_SOURCE_KEY);
        boolean isFromBckGround = Boolean.valueOf(uri.getQueryParameter(MoEHelperConstants.EXTRA_IS_FROM_BACKGROUND));
        // if user minimize app isFromBckGround=> False
        if (sourceName != null && sourceName.equals(MoEHelperConstants.NAVIGATION_SOURCE_NOTIFICATION)
                && isFromBckGround) {
            goToHome();
        } else {
            finish();
        }
    }

    /**
     * launching the splashactivity for registering the visitor id
     * passing the current intent with the key Constants.REDIRECT_INTENT
     */
    private void launchSplashActivity() {
        Intent intent = getIntent();
        intent.setClass(this, getClass());
        Intent splashActivityIntent = new Intent(this, SplashActivity.class);
        splashActivityIntent.putExtra(Constants.REDIRECT_INTENT, intent);
        startActivity(splashActivityIntent);
        finish();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleBackStack();
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
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void goToHome() {
        super.goToHome();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NavigationCodes.GO_TO_HOME) {
            handleBackStack();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}