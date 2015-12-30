package com.bigbasket.mobileapp.handler.network;

import com.bigbasket.mobileapp.interfaces.AppOperationAware;

import java.lang.ref.WeakReference;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class BBNetworkCallback<K> implements Callback<K> {

    private WeakReference<AppOperationAware> ctxWeakReference;
    private boolean finishOnFailure;

    public BBNetworkCallback(AppOperationAware ctx) {
        this.ctxWeakReference = new WeakReference<>(ctx);
    }

    public BBNetworkCallback(AppOperationAware ctx, boolean finishOnFailure) {
        this(ctx);
        this.finishOnFailure = finishOnFailure;
    }

    @Override
    public void onResponse(Response<K> response, Retrofit retrofit) {
        if (ctxWeakReference == null || ctxWeakReference.get() == null) return;
        if ((ctxWeakReference.get()).isSuspended()) return;
        if (!updateProgress()) return;
        if (response.isSuccess()) {
            onSuccess(response.body());
        } else {
            onFailure(response.code(), response.message());
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (ctxWeakReference == null || ctxWeakReference.get() == null) return;
        if ((ctxWeakReference.get()).isSuspended()) return;
        if (!updateProgress()) return;
        (ctxWeakReference.get()).getHandler().handleRetrofitError(t, finishOnFailure);
    }

    public void onFailure(int httpErrorCode, String msg) {
        (ctxWeakReference.get()).getHandler()
                .handleHttpError(httpErrorCode, msg, finishOnFailure);
    }

    public abstract void onSuccess(K k);

    public boolean updateProgress() {
        return true;
    }
}
