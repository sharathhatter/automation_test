package com.bigbasket.mobileapp.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.DeepLinkHandler;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.moe.pushlibrary.utils.MoEHelperConstants;

import java.util.HashMap;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

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

    private void setAppInBackGround(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

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
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }


    private class SilentDeepLinkHandler<T> extends BigBasketMessageHandler{

        public SilentDeepLinkHandler(T ctx) {
            super(ctx);
        }

        @Override
        public void sendEmptyMessage(int what, String message, boolean finish) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.ERROR_CODE, String.valueOf(what));
            map.put(TrackEventkeys.ERROR_MSG, message);
            trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
            goToHome(false);
        }

        @Override
        public void handleRetrofitError(RetrofitError error, String sourceName, boolean finish) {
            LogNotificationEvent(error);
            goToHome(false);
        }

    }

    private void LogNotificationEvent(RetrofitError error){
        HashMap<String, String> map = new HashMap<>();
        if(error.getResponse()!=null) {
            map.put(TrackEventkeys.ERROR_CODE, String.valueOf(error.getResponse().getStatus()));
            map.put(TrackEventkeys.ERROR_MSG, String.valueOf(error.getResponse().getReason()));
        }else {
            map.put(TrackEventkeys.ERROR_CODE, String.valueOf(error.getKind()));
            map.put(TrackEventkeys.ERROR_MSG, error.getMessage());
        }
        trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
    }

}