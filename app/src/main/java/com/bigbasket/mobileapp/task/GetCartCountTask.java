package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GetCartCountTask<T> {

    private static final String TAG = GetCartCountTask.class.getName();
    private T ctx;
    boolean failSilently;

    public GetCartCountTask(T ctx, boolean failSilently) {
        this.ctx = ctx;
        this.failSilently = failSilently;
    }

    public void startTask() {
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
            ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
            bigBasketApiService.cartSummary(new Callback<CartSummaryApiResponse>() {
                @Override
                public void success(CartSummaryApiResponse cartSummaryApiResponse, Response response) {
                    if (((CancelableAware) ctx).isSuspended()) {
                        return;
                    } else {
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                    }
                    switch (cartSummaryApiResponse.status) {
                        case Constants.OK:
                            ((CartInfoAware) ctx).setCartInfo(cartSummaryApiResponse.cartSummaryApiResponseContent.cartSummary);
                            ((CartInfoAware) ctx).updateUIForCartInfo();
                            break;
                        default:
                            if (!failSilently) {
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(cartSummaryApiResponse.getErrorTypeAsInt(),
                                        cartSummaryApiResponse.message);
                            }
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (((CancelableAware) ctx).isSuspended()) {
                        return;
                    } else {
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                    }
                    if (!failSilently) {
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                }
            });
        }
    }
}
