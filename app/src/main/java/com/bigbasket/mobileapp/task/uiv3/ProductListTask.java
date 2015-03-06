package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.ProductListApiResponseCallback;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;

public class ProductListTask<T> {

    private HashMap<String, String> paramMap;
    private int page;
    protected T ctx;

    public ProductListTask(T ctx, HashMap<String, String> paramMap) {
        this(1, ctx, paramMap);
    }

    public ProductListTask(int page, T ctx, HashMap<String, String> paramMap) {
        this.page = page;
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
        if (page > 1) {
            paramMap.put(Constants.CURRENT_PAGE, String.valueOf(page));
        }

        if (page == 1) {
            ((ProgressIndicationAware) ctx).showProgressView();
        }
        bigBasketApiService.productListUrl(paramMap, new ProductListApiResponseCallback<>(page, ctx));
    }
}
