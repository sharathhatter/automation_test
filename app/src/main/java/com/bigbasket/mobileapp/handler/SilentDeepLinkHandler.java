package com.bigbasket.mobileapp.handler;

import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

public class SilentDeepLinkHandler<T extends AppOperationAware & ApiErrorAware>
        extends BigBasketMessageHandler {

    private T ctx;

    @SuppressWarnings("unchecked")
    public SilentDeepLinkHandler(T ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public void sendEmptyMessage(int what, String message, boolean finish) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.ERROR_CODE, String.valueOf(what));
        map.put(TrackEventkeys.ERROR_MSG, message);
        ((AppOperationAware) ctx).getCurrentActivity().trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
        ((AppOperationAware) ctx).getCurrentActivity().goToHome();
    }

    @Override
    public void handleRetrofitError(Throwable t, boolean finish) {
        logNotificationEvent(0, "Network Error");
        ((AppOperationAware) ctx).getCurrentActivity().goToHome();
    }

    public void handleHttpError(int errorCode, String reasonPhrase, boolean finish) {
        logNotificationEvent(errorCode, reasonPhrase);
        ((AppOperationAware) ctx).getCurrentActivity().goToHome();
    }

    private void logNotificationEvent(int code, String msg) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.ERROR_CODE, String.valueOf(code));
        map.put(TrackEventkeys.ERROR_MSG, String.valueOf(msg));
        ((AppOperationAware) ctx).getCurrentActivity().trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
    }

}

