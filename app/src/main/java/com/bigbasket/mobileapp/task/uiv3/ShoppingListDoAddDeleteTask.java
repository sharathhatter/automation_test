package com.bigbasket.mobileapp.task.uiv3;

import android.util.Log;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShoppingListDoAddDeleteTask<T> {
    private static final String TAG = ShoppingListDoAddDeleteTask.class.getName();
    private List<ShoppingListName> selectedShoppingListNames;
    private T ctx;
    private ShoppingListOption shoppingListOption;

    public ShoppingListDoAddDeleteTask(T ctx, List<ShoppingListName> selectedShoppingListNames,
                                       ShoppingListOption shoppingListOption) {
        this.ctx = ctx;
        this.selectedShoppingListNames = selectedShoppingListNames;
        this.shoppingListOption = shoppingListOption;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());
        String selectedProductId = ((ShoppingListNamesAware) ctx).getSelectedProductId();
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        switch (shoppingListOption) {
            case ADD_TO_LIST:
                List<String> slugList = new ArrayList<>();
                for (ShoppingListName shoppingListName : selectedShoppingListNames) {
                    slugList.add(shoppingListName.getSlug());
                }
                bigBasketApiService.addItemToShoppingList(selectedProductId, new Gson().toJson(slugList),
                        new ShoppingListOperationApiResponseCallback());
                break;
            case DELETE_ITEM:
                bigBasketApiService.deleteItemFromShoppingList(selectedProductId, selectedShoppingListNames.get(0).getSlug(),
                        new ShoppingListOperationApiResponseCallback());
                break;
        }
    }

    private class ShoppingListOperationApiResponseCallback implements Callback<OldBaseApiResponse> {

        @Override
        public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
            if (((CancelableAware) ctx).isSuspended()) {
                return;
            } else {
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
            switch (oldBaseApiResponse.status) {
                case Constants.OK:
                    switch (shoppingListOption) {
                        case ADD_TO_LIST:
                            ((HandlerAware) ctx).getHandler().sendEmptyMessage(NavigationCodes.ADD_TO_SHOPPINGLIST_OK, null);
                            Log.d(TAG, "Sending message: MessageCode.ADD_TO_SHOPPINGLIST_OK");
                            break;
                        case DELETE_ITEM:
                            ((HandlerAware) ctx).getHandler().sendEmptyMessage(NavigationCodes.DELETE_FROM_SHOPPING_LIST_OK, null);
                            ((ShoppingListNamesAware) ctx).postShoppingListItemDeleteOperation();
                            Log.d(TAG, "Sending message: MessageCode.DELETE_FROM_SHOPPING_LIST_OK");
                            break;
                    }
                    break;
                default:
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                            oldBaseApiResponse.message);
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
    }
}
