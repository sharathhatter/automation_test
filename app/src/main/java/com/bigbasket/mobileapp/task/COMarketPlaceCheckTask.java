package com.bigbasket.mobileapp.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;


public class COMarketPlaceCheckTask extends AsyncTask<String, Long, Void> {


    private static final String TAG = COMarketPlaceCheckTask.class.getName();
    private ProgressDialog progressDialog;
    private HttpOperationResult httpOperationResult;
    private BaseActivity activity;

    public COMarketPlaceCheckTask(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (activity.checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(activity);
            HttpRequestData httpRequestData = new HttpRequestData(MobileApiUrl.getBaseAPIUrl() + Constants.CO_BASKET_CHECK,
                    null, false, authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpGet(httpRequestData);
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
            progressDialog = ProgressDialog.show(activity, "", "Please wait", true, false);
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
                String responseJson = httpOperationResult.getReponseString();
                JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
                int status = jsonObject.get(Constants.STATUS).getAsInt();
                if (status == 0) {
                    JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                    Gson gson = new Gson();
                    final MarketPlace marketPlace = gson.fromJson(responseJsonObject, MarketPlace.class);
                    // call interface method
                    //((COMarketPlaceAware) activity).setMarketPlaceInfo(marketPlace);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((COMarketPlaceAware) activity).onCoMarketPlaceSuccess(marketPlace);
                        }
                    });

                } else {
                    String msg = status == ExceptionUtil.INTERNAL_SERVER_ERROR ? activity.getResources().getString(R.string.INTERNAL_SERVER_ERROR) :
                            jsonObject.get(Constants.MESSAGE).getAsString();
                    activity.showAlertDialogFinish(activity, null, msg);
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
