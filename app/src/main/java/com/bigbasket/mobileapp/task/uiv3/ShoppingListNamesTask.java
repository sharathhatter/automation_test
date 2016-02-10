package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit2.Call;

public class ShoppingListNamesTask<T extends AppOperationAware> {
    private T ctx;
    private boolean showSystem;
    private String navigationContext;


    public ShoppingListNamesTask(T ctx, boolean showSystem, String navigationContext) {
        this.ctx = ctx;
        this.showSystem = showSystem;
        this.navigationContext = navigationContext;
    }

    public void startTask() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(ctx.getCurrentActivity());
        ctx.showProgressDialog("Please wait...");
        Call<GetShoppingListsApiResponse> call = bigBasketApiService.getShoppingLists(navigationContext, showSystem ? "1" : "0");
        call.enqueue(new BBNetworkCallback<GetShoppingListsApiResponse>(ctx, true) {
                         @Override
                         public void onSuccess(GetShoppingListsApiResponse getShoppingListsApiResponse) {
                             switch (getShoppingListsApiResponse.status) {
                                 case Constants.OK:
                                     ((ShoppingListNamesAware) ctx).onShoppingListFetched(getShoppingListsApiResponse.shoppingListNames);
                                     break;
                                 default:
                                     ctx.getHandler().sendEmptyMessage(getShoppingListsApiResponse.getErrorTypeAsInt(),
                                             getShoppingListsApiResponse.message, true);
                                     break;
                             }
                         }

                         @Override
                         public boolean updateProgress() {
                             try {
                                 ctx.hideProgressDialog();
                                 return true;
                             } catch (IllegalArgumentException e) {
                                 return false;
                             }
                         }

                     }

        );
    }
}

