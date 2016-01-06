package com.bigbasket.mobileapp.task.uiv3;

import android.util.Log;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;

public class ShoppingListDoAddDeleteTask<T extends AppOperationAware> {
    private static final String TAG = ShoppingListDoAddDeleteTask.class.getName();
    private List<ShoppingListName> selectedShoppingListNames;
    private T ctx;
    private
    @ShoppingListOption.Method
    int shoppingListOption;

    public ShoppingListDoAddDeleteTask(T ctx, List<ShoppingListName> selectedShoppingListNames,
                                       @ShoppingListOption.Method int shoppingListOption) {
        this.ctx = ctx;
        this.selectedShoppingListNames = selectedShoppingListNames;
        this.shoppingListOption = shoppingListOption;
    }

    public void startTask() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(ctx.getCurrentActivity());
        String selectedProductId = ((ShoppingListNamesAware) ctx).getSelectedProductId();
        ctx.showProgressDialog("Please wait...");
        Call<OldBaseApiResponse> call = null;
        switch (shoppingListOption) {
            case ShoppingListOption.ADD_TO_LIST:
                List<String> slugList = new ArrayList<>();
                for (ShoppingListName shoppingListName : selectedShoppingListNames) {
                    slugList.add(shoppingListName.getSlug());
                }
                call = bigBasketApiService.addItemToShoppingList(ctx.getCurrentActivity().getCurrentScreenName(),
                        selectedProductId, new Gson().toJson(slugList));
                break;
            case ShoppingListOption.DELETE_ITEM:
                call = bigBasketApiService.deleteItemFromShoppingList(ctx.getCurrentActivity().getPreviousScreenName(),
                        selectedProductId, selectedShoppingListNames.get(0).getSlug());
                break;
        }
        if (call != null) {
            call.enqueue(new ShoppingListOperationApiResponseCallback(ctx));
        }
    }

    private class ShoppingListOperationApiResponseCallback extends BBNetworkCallback<OldBaseApiResponse> {
        public ShoppingListOperationApiResponseCallback(AppOperationAware ctx) {
            super(ctx);
        }

        @Override
        public void onSuccess(OldBaseApiResponse oldBaseApiResponse) {
            switch (oldBaseApiResponse.status) {
                case Constants.OK:
                    switch (shoppingListOption) {
                        case ShoppingListOption.ADD_TO_LIST:
                            ctx.getHandler().sendEmptyMessage(NavigationCodes.ADD_TO_SHOPPINGLIST_OK, null);
                            ((ShoppingListNamesAware) ctx).postAddToShoppingListOperation();
                            Log.d(TAG, "Sending message: MessageCode.ADD_TO_SHOPPINGLIST_OK");
                            break;
                        case ShoppingListOption.DELETE_ITEM:
                            ctx.getHandler().sendEmptyMessage(NavigationCodes.DELETE_FROM_SHOPPING_LIST_OK, null);
                            ((ShoppingListNamesAware) ctx).postShoppingListItemDeleteOperation();
                            Log.d(TAG, "Sending message: MessageCode.DELETE_FROM_SHOPPING_LIST_OK");
                            break;
                    }
                    break;
                default:
                    ctx.getHandler().sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                            oldBaseApiResponse.message);
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
}
