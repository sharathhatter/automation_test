package com.bigbasket.mobileapp.task;

import android.os.AsyncTask;
import android.util.Log;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;

import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class ShoppingListDoOperationTask<T> extends AsyncTask<String, Long, Void> {

    private static final String TAG = ShoppingListDoOperationTask.class.getName();
    private String url;
    private List<String> selectedShoppingListNameSlugs;
    private T ctx;
    private ShoppingListOption shoppingListOption;
    private HttpOperationResult httpOperationResult;

    public ShoppingListDoOperationTask(T ctx, String url,
                                       List<String> selectedShoppingListNameSlugs,
                                       ShoppingListOption shoppingListOption) {
        this.ctx = ctx;
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
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            String selectedProductId = ((ShoppingListNamesAware) ctx).getSelectedProductId();
            HashMap<String, String> requestParams = new HashMap<>();
            requestParams.put("product_id", selectedProductId);
            switch (shoppingListOption) {
                case ADD_TO_LIST:
                    try {
                        JSONArray jsonArray = new JSONArray(selectedShoppingListNameSlugs.toString());
                        requestParams.put("slugs", jsonArray.toString());
                    } catch (JSONException e) {
                    }
                    break;
                case DELETE_ITEM:
                    requestParams.put(Constants.SLUG, selectedShoppingListNameSlugs.get(0));
                    break;
            }
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, requestParams, true, authParameters.getBbAuthToken(),
                    authParameters.getVisitorId(), authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");

        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (((CancelableAware) ctx).isSuspended()) {
            cancel(true);
        } else {
            ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (((CancelableAware) ctx).isSuspended()) {
            return;
        } else {
            try {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        if (httpOperationResult != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                try {
                    JSONObject jsonObject = new JSONObject(httpOperationResult.getReponseString());
                    httpOperationResult.setJsonObject(jsonObject);
                } catch (JSONException e) {
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                    return;
                }
                if (httpOperationResult.getJsonObject().optString(Constants.STATUS).equals("OK")) {
                    switch (shoppingListOption) {
                        case ADD_TO_LIST:
                            ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.ADD_TO_SHOPPINGLIST_OK);
                            Log.d(TAG, "Sending message: MessageCode.ADD_TO_SHOPPINGLIST_OK");
                            break;
                        case DELETE_ITEM:
                            ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.DELETE_FROM_SHOPPING_LIST_OK);
                            ((ShoppingListNamesAware) ctx).postShoppingListItemDeleteOperation();
                            Log.d(TAG, "Sending message: MessageCode.DELETE_FROM_SHOPPING_LIST_OK");
                            break;
                    }
                } else {
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                }

            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }

        super.onPostExecute(result);
    }

}
