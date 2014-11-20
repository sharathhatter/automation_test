package com.bigbasket.mobileapp.task;

import android.os.AsyncTask;
import android.util.Log;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;


public class COMarketPlaceCheckTask<T> extends AsyncTask<String, Long, Void> {


    private static final String TAG = COMarketPlaceCheckTask.class.getName();
    private HttpOperationResult httpOperationResult;
    private T ctx;

    public COMarketPlaceCheckTask(T ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity());
            HttpRequestData httpRequestData = new HttpRequestData(MobileApiUrl.getBaseAPIUrl() + Constants.CO_BASKET_CHECK,
                    null, false, authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpGet(httpRequestData);
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
                String responseJson = httpOperationResult.getReponseString();
                JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
                int status = jsonObject.get(Constants.STATUS).getAsInt();
                if (status == 0) {
                    JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                    Gson gson = new Gson();
                    final MarketPlace marketPlace = gson.fromJson(responseJsonObject, MarketPlace.class);
                    // call interface method
                    ((COMarketPlaceAware) ctx).onCoMarketPlaceSuccess(marketPlace);

                } else {
                    String msg = status == ExceptionUtil.INTERNAL_SERVER_ERROR ? "Server Error" :
                            jsonObject.get(Constants.MESSAGE).getAsString();
                    // TODO : Improve handling
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
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
