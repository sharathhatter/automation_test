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
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class ShoppingListNamesTask<T> extends AsyncTask<String, Long, Void> {
    private static final String TAG = ShoppingListNamesTask.class.getName();
    private HttpOperationResult httpOperationResult;
    private String url;
    private T ctx;
    private boolean showSystem;

    public ShoppingListNamesTask(T ctx, String url, boolean showSystem) {
        this.ctx = ctx;
        this.url = url;
        this.showSystem = showSystem;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity());
            HashMap<String, String> httpPostParams = new HashMap<String, String>() {
                {
                    put(Constants.SYSTEM, showSystem ? "1" : "0");
                }
            };
            HttpRequestData httpRequestData = new HttpRequestData(url, httpPostParams, true,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
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
        if (httpOperationResult != null && httpOperationResult.getReponseString() != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                String responseString = httpOperationResult.getReponseString();
                JsonElement responseJsonElement = new JsonParser().parse(responseString);
                ((ShoppingListNamesAware) ctx).setShoppingListNames(ParserUtil.parseShoppingList(responseJsonElement.getAsJsonObject()));
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.GET_SHOPPINGLIST_NAMES_OK);
                Log.d(TAG, "Sending message: MessageCode.GET_SHOPPINGLIST_NAMES_OK");
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

