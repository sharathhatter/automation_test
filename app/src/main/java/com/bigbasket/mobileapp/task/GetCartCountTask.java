package com.bigbasket.mobileapp.task;

import android.util.Log;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.util.Constants;

import java.lang.ref.WeakReference;

import retrofit.RetrofitError;

public class GetCartCountTask<T> {

    private WeakReference<T> ctx;

    public GetCartCountTask(T ctx) {
        this.ctx = new WeakReference<>(ctx);
    }

    public void startTask() {
        if (ctx.get() == null) return;
        if (((ConnectivityAware) ctx.get()).checkInternetConnection()) {
            Log.d("BigBasket", "Doing network call to sync cart");
            final BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx.get()).getCurrentActivity());
            new Thread() {
                @Override
                public void run() {
                    try {
                        final CartSummaryApiResponse cartSummaryApiResponse = bigBasketApiService.cartSummary();
                        if (ctx.get() != null && cartSummaryApiResponse != null) {
                            if (((CancelableAware) ctx.get()).isSuspended()) {
                                return;
                            }
                            switch (cartSummaryApiResponse.status) {
                                case Constants.OK:
                                    if (ctx.get() != null) {
                                        ((ActivityAware) ctx.get()).getCurrentActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (ctx.get() != null) {
                                                    ((CartInfoAware) ctx.get()).setCartSummary(cartSummaryApiResponse.cartSummaryApiResponseContent.cartSummary);
                                                }
                                                if (ctx.get() != null) {
                                                    ((CartInfoAware) ctx.get()).updateUIForCartInfo();
                                                }
                                            }
                                        });
                                    }
                                    break;
                            }
                        }
                    } catch (RetrofitError | NullPointerException e) {

                    }
                }
            }.start();
        }
    }
}
