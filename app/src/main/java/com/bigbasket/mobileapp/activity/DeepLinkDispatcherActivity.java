package com.bigbasket.mobileapp.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.moe.pushlibrary.utils.MoEHelperConstants;

import java.util.List;

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

    private void handleBackStack(){
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        String sourceName = uri.getQueryParameter(MoEHelperConstants.NAVIGATION_SOURCE_KEY);
        if(sourceName.equals(MoEHelperConstants.NAVIGATION_SOURCE_NOTIFICATION)){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        }else {
            finish();
        }
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * MoEHelperUtils.dumpIntentExtras for dumping all extras
     * MoEHelperConstants.NAVIGATION_*
     *
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
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }
}