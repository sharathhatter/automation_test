package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.ProductListApiResponseCallback;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;

import java.util.HashMap;

public class ProductListTask<T> {

    protected T ctx;
    private HashMap<String, String> paramMap;

    public ProductListTask(T ctx, HashMap<String, String> paramMap) {
        this.ctx = ctx;
        this.paramMap = paramMap;
    }


    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());

        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.productList(paramMap, new ProductListApiResponseCallback<>(ctx, false));
    }
}
