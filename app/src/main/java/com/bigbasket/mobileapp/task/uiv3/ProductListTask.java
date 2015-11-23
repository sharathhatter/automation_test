package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.ProductListApiResponseCallback;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.product.ProductTabData;

import java.util.HashMap;

import retrofit.Call;

public class ProductListTask<T extends AppOperationAware> {

    protected T ctx;
    private String navigationCtx;
    private HashMap<String, String> paramMap;
    private boolean isFilterOrSortApplied;

    public ProductListTask(T ctx, HashMap<String, String> paramMap, String navigationCtx,
                           boolean isFilterOrSortApplied) {
        this.ctx = ctx;
        this.paramMap = paramMap;
        this.navigationCtx = navigationCtx;
        this.isFilterOrSortApplied = isFilterOrSortApplied;
    }


    public void startTask() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(ctx.getCurrentActivity());

        if (ctx.isSuspended()) return;
        ctx.showProgressDialog("Please wait...");
        Call<ApiResponse<ProductTabData>> call =
                bigBasketApiService.productList(navigationCtx, paramMap);
        call.enqueue(new ProductListApiResponseCallback<>(ctx, false, isFilterOrSortApplied));
    }
}
