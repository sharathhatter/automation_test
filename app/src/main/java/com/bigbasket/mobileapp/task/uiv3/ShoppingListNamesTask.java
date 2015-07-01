package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShoppingListNamesTask<T> {
    private T ctx;
    private boolean showSystem;

    public ShoppingListNamesTask(T ctx, boolean showSystem) {
        this.ctx = ctx;
        this.showSystem = showSystem;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.getShoppingLists(showSystem ? "1" : "0", new Callback<GetShoppingListsApiResponse>() {
            @Override
            public void success(GetShoppingListsApiResponse getShoppingListsApiResponse, Response response) {

                if (((CancelableAware) ctx).isSuspended()) {
                    return;
                } else {
                    try {
                        ((ProgressIndicationAware) ctx).hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                }
                switch (getShoppingListsApiResponse.status) {
                    case Constants.OK:
                        ((ShoppingListNamesAware) ctx).onShoppingListFetched(getShoppingListsApiResponse.shoppingListNames);
                        break;
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(getShoppingListsApiResponse.getErrorTypeAsInt(),
                                getShoppingListsApiResponse.message, true);
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
                ((HandlerAware) ctx).getHandler().handleRetrofitError(error, true);
            }
        });
    }
}

