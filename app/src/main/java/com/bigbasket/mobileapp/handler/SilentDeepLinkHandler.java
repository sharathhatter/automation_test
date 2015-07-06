package com.bigbasket.mobileapp.handler;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

import retrofit.RetrofitError;

/**
 * Created by jugal on 6/7/15.
 */
public class SilentDeepLinkHandler<T> extends BigBasketMessageHandler {

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
        ((ActivityAware)ctx).getCurrentActivity().trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
        ((ActivityAware)ctx).getCurrentActivity().goToHome(false);
    }

    @Override
    public void handleRetrofitError(RetrofitError error, String sourceName, boolean finish) {
        logNotificationEvent(error);
        ((ActivityAware)ctx).getCurrentActivity().goToHome(false);
    }

    private void logNotificationEvent(RetrofitError error) {
        HashMap<String, String> map = new HashMap<>();
        if (error.getResponse() != null) {
            map.put(TrackEventkeys.ERROR_CODE, String.valueOf(error.getResponse().getStatus()));
            map.put(TrackEventkeys.ERROR_MSG, String.valueOf(error.getResponse().getReason()));
        } else {
            map.put(TrackEventkeys.ERROR_CODE, String.valueOf(error.getKind()));
            map.put(TrackEventkeys.ERROR_MSG, error.getMessage());
        }
        ((ActivityAware)ctx).getCurrentActivity().trackEvent(TrackingAware.NOTIFICATION_ERROR, map);
    }

}

