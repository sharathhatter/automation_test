package com.bigbasket.mobileapp.task.uiv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
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

public class ProductListTask extends AsyncTask {
    private static final String TAG = ProductListTask.class.getName();

    private int page;
    private HttpOperationResult httpOperationResult;
    private ProductListAwareFragment fragment;
    private Context context;

    public ProductListTask(ProductListAwareFragment fragment) {
        this.page = 1;
        this.fragment = fragment;
        this.context = fragment.getActivity();
    }

    public ProductListTask(int page, ProductListAwareFragment fragment) {
        this.page = page;
        this.fragment = fragment;
        this.context = fragment.getActivity();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if (isCancelled()) {
            return null;
        }
        ProductQuery productQuery = fragment.getProductQuery();
        String url = fragment.getProductListUrl();
        if (productQuery != null) {
            if (page > 1) {
                productQuery.setPage(page);
            }
            List<NameValuePair> parameterList = productQuery.getAsNameValuePair();
            url += URLEncodedUtils.format(parameterList, "utf-8");
        }
        if (fragment.checkInternetConnection()) {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
            String visitorId = AuthParameters.getInstance(context).getVisitorId();
            String bbAuthToken = AuthParameters.getInstance(context).getBbAuthToken();
            String osVersion = prefer.getString("os", "");
            httpOperationResult = DataUtil.doHttpGet(url, new BasicCookieStore(), visitorId,
                    bbAuthToken, osVersion);
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
        } else if (page == 1) {
            fragment.showProgressView();
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (httpOperationResult != null) {
            if (isSuccessFull(httpOperationResult)) {
                ProductListDataAware productListDataAware = fragment;
                ProductListData productListData = ParserUtil.parseProductListData(httpOperationResult.getJsonObject());
                if (page > 1) {
                    ProductListData existingProductListData = fragment.getProductListData();
                    existingProductListData.setCurrentPage(productListData.getCurrentPage());
                    productListDataAware.updateProductList(productListData.getProducts());
                } else {
                    fragment.hideProgressView();
                    productListDataAware.setProductListData(productListData);
                    if (!TextUtils.isEmpty(productListData.getSortedOn())) {
                        productListDataAware.getProductListData().
                                setUserSortedOn(productListDataAware.getProductListData().getUserSortedOn());
                    }
                    productListDataAware.updateData();
                }
                Log.d(TAG, "Product list fetch and display completed");
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
