package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.ProductListApiResponseCallback;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.ProductQuery;

public class ProductListTask<T> {

    private int page;
    protected T ctx;

    public ProductListTask(T ctx) {
        this(1, ctx);
    }

    public ProductListTask(int page, T ctx) {
        this.page = page;
        this.ctx = ctx;
    }


    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());
        ProductQuery productQuery = ((ProductListDataAware) ctx).getProductQuery();
        if (page > 1) {
            productQuery.setPage(page);
        }

        if (page == 1) {
            ((ProgressIndicationAware) ctx).showProgressView();
        }
        bigBasketApiService.productListUrl(productQuery.getAsQueryMap(), new ProductListApiResponseCallback<>(page, ctx));
    }
}
