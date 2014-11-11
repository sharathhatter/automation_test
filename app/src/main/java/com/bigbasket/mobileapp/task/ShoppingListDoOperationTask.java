package com.bigbasket.mobileapp.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;

import java.util.List;

public class ShoppingListDoOperationTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = ShoppingListDoOperationTask.class.getName();
    private ProgressDialog progressDialog;
    private String url;
    private List<String> selectedShoppingListNameSlugs;
    private BaseActivity activity;
    private ShoppingListOption shoppingListOption;
    private HttpOperationResult httpOperationResult;

    public ShoppingListDoOperationTask(BaseActivity activity, String url,
                                       List<String> selectedShoppingListNameSlugs,
                                       ShoppingListOption shoppingListOption) {
        this.activity = activity;
        this.url = url;
        this.selectedShoppingListNameSlugs = selectedShoppingListNameSlugs;
        this.shoppingListOption = shoppingListOption;
    }

    @SuppressWarnings("unused")
    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (activity.checkInternetConnection()) {
            String selectedProductId = ((ShoppingListNamesAware) activity).getSelectedProductId();
            httpOperationResult = DataUtil.doHttpPostShoppingListOptions(activity, url, shoppingListOption, selectedShoppingListNameSlugs, selectedProductId);
        } else {
            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");

        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activity.isActivitySuspended()) {
            cancel(true);
        } else {
            progressDialog = ProgressDialog.show(activity, "", "Please wait");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException ex) {
                return;
            }
        } else {
            return;
        }
        if (httpOperationResult != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                if (httpOperationResult.getJsonObject().optString(Constants.STATUS).equals("OK")) {
                    switch (shoppingListOption) {
                        case ADD_TO_LIST:
                            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.ADD_TO_SHOPPINGLIST_OK);
                            Log.d(TAG, "Sending message: MessageCode.ADD_TO_SHOPPINGLIST_OK");
                            break;
                        case DELETE_ITEM:
                            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.DELETE_FROM_SHOPPING_LIST_OK);
                            ((ShoppingListNamesAware) activity).postShoppingListItemDeleteOperation();
                            Log.d(TAG, "Sending message: MessageCode.DELETE_FROM_SHOPPING_LIST_OK");
                            break;
                    }
                } else {
                    ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                }

            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }

        super.onPostExecute(result);
    }

}
