package com.bigbasket.mobileapp.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class COReserveQuantityCheckTask<T> extends AsyncTask<String, Long, Void> {

    private static final String TAG = COReserveQuantityCheckTask.class.getName();
    private static String URL = MobileApiUrl.getBaseAPIUrl() + Constants.CO_RESERVE_QTY;
    private HttpOperationResult httpOperationResult;
    private String pharmaPrescriptionId;
    private T ctx;


    public COReserveQuantityCheckTask(T ctx, String pharmaPrescriptionId) {
        this.ctx = ctx;
        this.pharmaPrescriptionId = pharmaPrescriptionId;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            HashMap<String, String> load = null;
            if (pharmaPrescriptionId != null) {
                load = new HashMap<>();
                load.put(Constants.PHARMA_PRESCRIPTION_ID, pharmaPrescriptionId);
            }
            //fragment.startAsyncActivity(URL, load, true, true, null);
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity());
            HttpRequestData httpRequestData = new HttpRequestData(URL, load, true,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
        } else {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
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

    @SuppressWarnings("unchecked")
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
            if (httpOperationResult.getResponseCode() == HttpStatus.SC_OK) {
                COReserveQuantity coReserveQuantity = ParserUtil.parseCoReserveQuantity(httpOperationResult.getReponseString());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity()).edit();
                editor.putString(Constants.POTENTIAL_ORDER_ID, String.valueOf(coReserveQuantity.getOrderId()));
                editor.commit();

                ((COReserveQuantityCheckAware) ctx).setCOReserveQuantity(coReserveQuantity);
                ((COReserveQuantityCheckAware) ctx).onCOReserveQuantityCheck();
                Log.d(TAG, "Calling on COReserveQuantityCheck()");

            } else if (httpOperationResult.getResponseCode() == HttpStatus.SC_UNAUTHORIZED) {
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.LOGIN_REQUIRED);
                Log.d(TAG, "Sending message: ApiErrorCodes.LOGIN_REQUIRED");

            } else {
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.SERVER_ERROR);
                Log.d(TAG, "Sending message: ApiErrorCodes.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.SERVER_ERROR);
            Log.d(TAG, "Sending message: ApiErrorCodes.SERVER_ERROR");
        }
        super.onPostExecute(result);
    }

}
