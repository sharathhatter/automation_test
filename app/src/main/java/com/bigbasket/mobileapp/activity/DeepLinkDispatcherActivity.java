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
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.moe.pushlibrary.utils.MoEHelperConstants;

public class DeepLinkDispatcherActivity extends BaseActivity implements InvoiceDataAware,
        HandlerAware {

    protected BigBasketMessageHandler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new SilentDeepLinkHandler<>(this);
        launchCorrespondingActivity();
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    private void launchCorrespondingActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        setNextScreenNavigationContext(TrackEventkeys.DEEP_LINK);
        int resultCode = DeepLinkHandler.handleDeepLink(this, uri);
        if (resultCode == DeepLinkHandler.LOGIN_REQUIRED) {
            showToast(getString(R.string.login_required));
            launchLogin(TrackEventkeys.NAVIGATION_CTX_DIALOG, uri);
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
        if (sourceName != null && sourceName.equals(MoEHelperConstants.NAVIGATION_SOURCE_NOTIFICATION)) {
            goToHome(false);
        } else {
            finish();
        }
    }

//    private void setAppInBackGround(final Context context) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean isInBackground = true;
//                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//                    List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//                    for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                            for (String activeProcess : processInfo.pkgList) {
//                                if (activeProcess.equals(context.getPackageName())) {
//                                    isInBackground = false;
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//                    ComponentName componentInfo = taskInfo.get(0).topActivity;
//                    if (componentInfo.getPackageName().equals(context.getPackageName())) {
//                        isInBackground = false;
//                    }
//                }
//            }
//        });
//    }

    /**
     * MoEHelperUtils.dumpIntentExtras for dumping all extras
     * MoEHelperConstants.NAVIGATION_*
     */

    @Override
    protected void onRestart() {
        super.onRestart();
        handleBackStack();
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
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

}