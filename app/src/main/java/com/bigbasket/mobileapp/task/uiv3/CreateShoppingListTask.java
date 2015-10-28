package com.bigbasket.mobileapp.task.uiv3;

import android.text.TextUtils;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.InputDialog;
import com.bigbasket.mobileapp.util.UIUtil;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateShoppingListTask<T> {
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
                            ((ActivityAware) ctx).getCurrentActivity().
                                    getResources().getString(R.string.error), ((ActivityAware) ctx).getCurrentActivity().
                                    getResources().getString(R.string.shoppingListNameAlphaNumeric),
                            Constants.NOT_ALPHANUMERIC_TXT_SHOPPING_LIST, null);
                }
            }
        }.show();
    }

    private void startTask(final String shoppingListName) {
        if (TextUtils.isEmpty(shoppingListName)) {
            Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                    "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog(((ActivityAware) ctx).getCurrentActivity().getString(R.string.please_wait));
        bigBasketApiService.createShoppingList(shoppingListName, "1", new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        ((TrackingAware) ctx).trackEvent(TrackingAware.SHOP_LST_CREATED, null);
                        ((ShoppingListNamesAware) ctx).onNewShoppingListCreated(shoppingListName);
                        break;
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message, true);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                ((HandlerAware) ctx).getHandler().handleRetrofitError(error, true);
            }
        });
    }
}
