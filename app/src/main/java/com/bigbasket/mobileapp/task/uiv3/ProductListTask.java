package com.bigbasket.mobileapp.task.uiv3;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.model.product.ProductQuery;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.ParserUtil;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProductListTask<T> extends AsyncTask {
    private static final String TAG = ProductListTask.class.getName();

    private int page;
    private HttpOperationResult httpOperationResult;
    private T ctx;

    public ProductListTask(T ctx) {
        this(1, ctx);
    }

    public ProductListTask(int page, T ctx) {
        this.page = page;
        this.ctx = ctx;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if (isCancelled()) {
            return null;
        }
        ProductQuery productQuery = ((ProductListDataAware) ctx).getProductQuery();
        String url = ((ProductListDataAware) ctx).getProductListUrl();
        if (productQuery != null) {
            if (page > 1) {
                productQuery.setPage(page);
            }
            List<NameValuePair> parameterList = productQuery.getAsNameValuePair();
            url += URLEncodedUtils.format(parameterList, "utf-8");
        }
        if (((ConnectivityAware) ctx).checkInternetConnection()) {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity());
            String visitorId = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity()).getVisitorId();
            String bbAuthToken = AuthParameters.getInstance(((ActivityAware) ctx).getCurrentActivity()).getBbAuthToken();
            String osVersion = prefer.getString("os", "");
            httpOperationResult = DataUtil.doHttpGet(url, new BasicCookieStore(), visitorId,
                    bbAuthToken, osVersion);
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
        } else if (page == 1) {
            ((ProgressIndicationAware) ctx).showProgressView();
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (((CancelableAware) ctx).isSuspended()) {
            return;
        } else {
            if (page == 1) {
                try {
                    ((ProgressIndicationAware) ctx).hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
        }
        if (httpOperationResult != null) {
            if (isSuccessFull(httpOperationResult)) {
                ProductListData productListData = ParserUtil.parseProductListData(httpOperationResult.getJsonObject());
                if (page > 1) {
                    ProductListData existingProductListData = ((ProductListDataAware) ctx).getProductListData();
                    existingProductListData.setCurrentPage(productListData.getCurrentPage());
                    ((ProductListDataAware) ctx).updateProductList(productListData.getProducts());
                } else {
                    ((ProductListDataAware) ctx).setProductListData(productListData);
                    if (!TextUtils.isEmpty(productListData.getSortedOn())) {
                        ((ProductListDataAware) ctx).getProductListData().
                                setUserSortedOn(((ProductListDataAware) ctx).getProductListData().getUserSortedOn());
                    }
                    ((ProductListDataAware) ctx).updateData();
                }
                Log.d(TAG, "Product list fetch and display completed");
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
    }

    private boolean isSuccessFull(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getResponseCode() == HttpStatus.SC_OK) {
            JSONObject jsonObject = httpOperationResult.getJsonObject();
            try {
                if (jsonObject != null) {
                    int status = jsonObject.getInt(Constants.STATUS);
                    return status == 0;
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
