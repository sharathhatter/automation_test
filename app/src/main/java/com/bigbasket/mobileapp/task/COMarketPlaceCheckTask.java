package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class COMarketPlaceCheckTask<T> {


    private static final String TAG = COMarketPlaceCheckTask.class.getName();
    private T ctx;

    public COMarketPlaceCheckTask(T ctx) {
        this.ctx = ctx;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.basketCheck(new Callback<ApiResponse<MarketPlace>>() {
            @Override
            public void success(ApiResponse<MarketPlace> marketPlaceApiResponse, Response response) {
                if (((CancelableAware) ctx).isSuspended()) {
                    return;
                } else {
                    try {
                        ((ProgressIndicationAware) ctx).hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                }
                switch (marketPlaceApiResponse.status) {
                    case 0:
                        ((COMarketPlaceAware) ctx).onCoMarketPlaceSuccess(marketPlaceApiResponse.apiResponseContent);
                        break;
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(marketPlaceApiResponse.status,
                                marketPlaceApiResponse.message);
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
                ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
            }
        });
    }
}
