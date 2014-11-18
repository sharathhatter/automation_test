package com.bigbasket.mobileapp.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;

public class GetCartCountTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = GetCartCountTask.class.getName();
    private ProgressDialog progressDialog;
    private HttpOperationResult httpOperationResult;
    private BaseActivity activity;
    private String url;

    public GetCartCountTask(BaseActivity activity) {
        this(activity, MobileApiUrl.getBaseAPIUrl() + Constants.C_SUMMARY);
    }

    public GetCartCountTask(BaseActivity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (activity.checkInternetConnection()) {
            httpOperationResult = DataUtil.doHttpGet(activity, url);
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
                CartSummary cartInfo = ParserUtil.parseGetCartCountResponse(httpOperationResult.getJsonObject());
                ((CartInfoAware) activity).setCartInfo(cartInfo);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((CartInfoAware) activity).updateUIForCartInfo();
                    }
                });
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
