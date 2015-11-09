package com.bigbasket.mobileapp.task.uiv3;

import android.text.TextUtils;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.InputDialog;
import com.bigbasket.mobileapp.util.UIUtil;

import retrofit.Call;

public class CreateShoppingListTask<T extends AppOperationAware> {
    private T ctx;

    public CreateShoppingListTask(T ctx) {
        this.ctx = ctx;
    }

    public void showDialog() {
        new InputDialog<T>(ctx, R.string.createList, R.string.cancel,
                R.string.createShoppingList, R.string.shoppingListNameDialogTextHint) {
            @Override
            public void onPositiveButtonClicked(String inputText) {
                if (UIUtil.isAlphaNumericString(inputText.trim())) {
                    startTask(inputText);
                } else {
                    ((ApiErrorAware) ctx).showApiErrorDialog(
                            ctx.getCurrentActivity().
                                    getResources().getString(R.string.error), ctx.getCurrentActivity().
                                    getResources().getString(R.string.shoppingListNameAlphaNumeric),
                            Constants.NOT_ALPHANUMERIC_TXT_SHOPPING_LIST, null);
                }
            }
        }.show();
    }

    private void startTask(final String shoppingListName) {
        if (TextUtils.isEmpty(shoppingListName)) {
            Toast.makeText(ctx.getCurrentActivity(),
                    "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        ctx.showProgressDialog(ctx.getCurrentActivity().getString(R.string.please_wait));
        Call<OldBaseApiResponse> call = bigBasketApiService.createShoppingList(shoppingListName, "1");
        call.enqueue(new BBNetworkCallback<OldBaseApiResponse>(ctx, true) {
            @Override
            public void onSuccess(OldBaseApiResponse oldBaseApiResponse) {
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        ((TrackingAware) ctx).trackEvent(TrackingAware.SHOP_LST_CREATED, null);
                        ((ShoppingListNamesAware) ctx).onNewShoppingListCreated(shoppingListName);
                        break;
                    default:
                        ctx.getHandler().sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message, true);
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
        });
    }
}
