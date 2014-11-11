package com.bigbasket.mobileapp.task.uiv3;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.*;
import org.apache.http.impl.client.BasicCookieStore;

public class COReserveQuantityCheckTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = "CoReserveQuantityTask";
    private static final String URL = MobileApiUrl.getBaseAPIUrl() + "co-reserve-quantity/";
    private BaseFragment fragment;
    private HttpOperationResult httpOperationResult;

    public COReserveQuantityCheckTask(BaseFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (fragment.checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(fragment.getActivity());

            HttpRequestData httpRequestData = new HttpRequestData(URL, null, true,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
        } else {
            ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
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
            fragment.showProgressDialog(fragment.getString(R.string.please_wait));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPostExecute(Void result) {
        if (fragment.isSuspended()) {
            return;
        } else {
            try {
                fragment.hideProgressDialog();
            } catch (IllegalArgumentException ex) {
                return;
            }
        }
        if (httpOperationResult != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                COReserveQuantity coReserveQuantity = ParserUtil.parseCoReserveQuantity(httpOperationResult.getReponseString());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity()).edit();
                editor.putString(Constants.POTENTIAL_ORDER_ID, String.valueOf(coReserveQuantity.getOrderId()));
                editor.commit();

                ((COReserveQuantityCheckAware) fragment).setCOReserveQuantity(coReserveQuantity);
                ((COReserveQuantityCheckAware) fragment).onCOReserveQuantityCheck();
                Log.d(TAG, "Calling on COReserveQuantityCheck()");

            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }
        super.onPostExecute(result);
    }

}

