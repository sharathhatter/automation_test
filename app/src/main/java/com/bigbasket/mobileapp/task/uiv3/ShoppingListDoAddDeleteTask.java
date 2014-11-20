package com.bigbasket.mobileapp.task.uiv3;

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
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MessageCode;
import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShoppingListDoAddDeleteTask<T> extends AsyncTask<String, Long, Void> {
    private static final String TAG = ShoppingListDoAddDeleteTask.class.getName();
    private String url;
    private List<ShoppingListName> selectedShoppingListNames;
    private T ctx;
    private ShoppingListOption shoppingListOption;
    private HttpOperationResult httpOperationResult;

    public ShoppingListDoAddDeleteTask(T ctx, String url,
                                       List<ShoppingListName> selectedShoppingListNames,
                                       ShoppingListOption shoppingListOption) {
        this.ctx = ctx;
        this.url = url;
        this.selectedShoppingListNames = selectedShoppingListNames;
        this.shoppingListOption = shoppingListOption;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            String selectedProductId = ((ShoppingListNamesAware) ctx).getSelectedProductId();
            HashMap<String, String> paramMap = new HashMap<>();
            List<String> slugList = new ArrayList<>();
            for (ShoppingListName shoppingListName : selectedShoppingListNames) {
                slugList.add(shoppingListName.getSlug());
            }
            Gson gson = new Gson();
            paramMap.put("product_id", selectedProductId);
            paramMap.put("slugs", gson.toJson(slugList));
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, paramMap, true, authParameters.getBbAuthToken(),
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
            if (httpOperationResult.getResponseCode() == HttpStatus.SC_OK || httpOperationResult.getResponseCode() == HttpStatus.SC_ACCEPTED) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(httpOperationResult.getReponseString());
                } catch (JSONException e) {
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR due to invalid json");
                    return;
                }
                if (jsonObject.optString(Constants.STATUS).equals("OK")) {
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

            } else if (httpOperationResult.getResponseCode() == HttpStatus.SC_UNAUTHORIZED) {
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
