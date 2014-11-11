package com.bigbasket.mobileapp.task.uiv3;

import android.os.AsyncTask;
import android.util.Log;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.*;
import org.apache.http.impl.client.BasicCookieStore;


public class CartCountTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = CartCountTask.class.getName();
    private HttpOperationResult httpOperationResult;
    private BaseFragment fragment;
    private String url;

    public CartCountTask(BaseFragment fragment) {
        this(fragment, MobileApiUrl.getBaseAPIUrl() + Constants.C_SUMMARY);
    }

    public CartCountTask(BaseFragment fragment, String url) {
        this.fragment = fragment;
        this.url = url;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (fragment.checkInternetConnection()) {
            AuthParameters authParameters = AuthParameters.getInstance(fragment.getActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, null, false, authParameters.getBbAuthToken(),
                    authParameters.getVisitorId(), authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpGet(httpRequestData);
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
                CartSummary cartInfo = ParserUtil.parseGetCartCountResponse(httpOperationResult.getReponseString());
                fragment.setCartInfo(cartInfo);
                fragment.updateUIForCartInfo();
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
