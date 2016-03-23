package com.bigbasket.mobileapp.handler.network;

import com.bigbasket.mobileapp.interfaces.AppOperationAware;

import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    public void onResponse(Call<K> call, Response<K> response) {
        if (ctxWeakReference == null || ctxWeakReference.get() == null
                || (call != null && call.isCanceled())) return;
        if ((ctxWeakReference.get()).isSuspended()) return;
        if (!updateProgress()) return;
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            onFailure(response.code(), response.message());
        }
    }

    @Override
    public void onFailure(Call<K> call, Throwable t) {
        if (ctxWeakReference == null || ctxWeakReference.get() == null
                || (call != null && call.isCanceled())) return;
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
