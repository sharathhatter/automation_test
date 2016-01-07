package com.bigbasket.mobileapp.task;

import android.util.Log;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.util.Constants;

import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit.Call;
import retrofit.Response;


public class GetCartCountTask<T> {

    private WeakReference<T> ctx;

    public GetCartCountTask(T ctx) {
        this.ctx = new WeakReference<>(ctx);
    }

    public void startTask() {
        if (ctx.get() == null) return;
        if (((AppOperationAware) ctx.get()).checkInternetConnection()) {
            Log.d("BigBasket", "Doing network call to sync cart");
            final BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((AppOperationAware) ctx.get()).getCurrentActivity());
            new Thread() {
                @Override
                public void run() {
                    try {
                        Call<CartSummaryApiResponse> call =
                                bigBasketApiService.cartSummary(((AppOperationAware) ctx.get()).getCurrentActivity().getCurrentScreenName());
                        Response<CartSummaryApiResponse> response = call.execute();
                        if (!response.isSuccess()) return;
                        final CartSummaryApiResponse cartSummaryApiResponse = response.body();
                        if (ctx.get() != null && cartSummaryApiResponse != null) {
                            if (((AppOperationAware) ctx.get()).isSuspended()) {
                                return;
                            }
                            switch (cartSummaryApiResponse.status) {
                                case Constants.OK:
                                    if (ctx.get() != null) {
                                        ((AppOperationAware) ctx.get()).getCurrentActivity().runOnUiThread(new Runnable() {
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
                    } catch (IOException | NullPointerException e) {
// Fail silently
                    }
                }
            }.start();
        }
    }
}
