package com.bigbasket.mobileapp.task.uiv3;

import android.os.AsyncTask;
import android.util.Log;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class ShoppingListNamesTask extends AsyncTask<String, Long, Void> {
    private static final String TAG = ShoppingListNamesTask.class.getName();
    private HttpOperationResult httpOperationResult;
    private String url;
    private BaseFragment fragment;
    private boolean showSystem;

    public ShoppingListNamesTask(BaseFragment fragment, String url, boolean showSystem) {
        this.fragment = fragment;
        this.url = url;
        this.showSystem = showSystem;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (fragment.checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(fragment.getActivity());
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
            fragment.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");

        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fragment.isSuspended()) {
            cancel(true);
        } else {
            fragment.showProgressDialog("Please wait...");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (fragment.isSuspended()) {
            return;
        } else {
            fragment.hideProgressDialog();
        }
        if (httpOperationResult != null && httpOperationResult.getReponseString() != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                String responseString = httpOperationResult.getReponseString();
                JsonElement responseJsonElement = new JsonParser().parse(responseString);
                ((ShoppingListNamesAware) fragment).setShoppingListNames(ParserUtil.parseShoppingList(responseJsonElement.getAsJsonObject()));
                fragment.getHandler().sendEmptyMessage(MessageCode.GET_SHOPPINGLIST_NAMES_OK);
                Log.d(TAG, "Sending message: MessageCode.GET_SHOPPINGLIST_NAMES_OK");
            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                fragment.getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }
        super.onPostExecute(result);
    }

}

