package com.bigbasket.mobileapp.task;

import android.util.Log;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MessageCode;

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
        bigBasketApiService.checkoutBasketCheck(new Callback<ApiResponse<MarketPlace>>() {
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
                        String msg = marketPlaceApiResponse.status == ExceptionUtil.INTERNAL_SERVER_ERROR ?
                                "Server Error" : marketPlaceApiResponse.message;
                        // TODO : Improve handling
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
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
                // TODO : Improve handling
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }
        });
    }
}
