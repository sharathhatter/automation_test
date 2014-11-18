package com.bigbasket.mobileapp.task;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class COReserveQuantityCheckTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = COReserveQuantityCheckTask.class.getName();
    private static String URL = MobileApiUrl.getBaseAPIUrl() + Constants.CO_RESERVE_QTY;
    private ProgressDialog progressDialog;
    private HttpOperationResult httpOperationResult;
    private String pharmaPrescriptionId;
    private BaseActivity activity;


    public COReserveQuantityCheckTask(BaseActivity activity, String pharmaPrescriptionId) {
        this.activity = activity;
        this.pharmaPrescriptionId = pharmaPrescriptionId;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (activity.checkInternetConnection()) {
            HashMap<String, String> load = null;
            if (pharmaPrescriptionId != null) {
                load = new HashMap<>();
                load.put(Constants.PHARMA_PRESCRIPTION_ID, pharmaPrescriptionId);
            }
            //fragment.startAsyncActivity(URL, load, true, true, null);
            AuthParameters authParameters = AuthParameters.getInstance(activity);
            HttpRequestData httpRequestData = new HttpRequestData(URL, load, true,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
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

    @SuppressWarnings("unchecked")
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
                COReserveQuantity coReserveQuantity = ParserUtil.parseCoReserveQuantity(httpOperationResult.getReponseString());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
                editor.putString(Constants.POTENTIAL_ORDER_ID, String.valueOf(coReserveQuantity.getOrderId()));
                editor.commit();

                ((COReserveQuantityCheckAware) activity).setCOReserveQuantity(coReserveQuantity);
                ((COReserveQuantityCheckAware) activity).onCOReserveQuantityCheck();
                Log.d(TAG, "Calling on COReserveQuantityCheck()");

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
