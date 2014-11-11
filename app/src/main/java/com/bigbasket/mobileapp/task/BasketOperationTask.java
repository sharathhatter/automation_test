package com.bigbasket.mobileapp.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class BasketOperationTask extends AsyncTask<String, Long, Void> {

    private static final String TAG = BasketOperationTask.class.getName();
    private BaseFragment fragment;
    private String url;
    private HttpOperationResult httpOperationResult;
    private String productId;
    private Product product;
    private BasketOperation basketOperation;
    private String qty;
    private TextView basketCountTextView;
    private ImageView imgIncQty;
    private ImageView imgDecQty;
    private Button btnAddToBasket;
    private EditText editTextQty;

    public BasketOperationTask(BaseFragment fragment, String url,
                               BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty) {
        this(fragment, url, basketOperation, product, basketCountTextView, imgDecQty, imgIncQty,
                btnAddToBasket, editTextQty, "1");
    }

    public BasketOperationTask(BaseFragment fragment, String url,
                               BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty, String qty) {
        this.fragment = fragment;
        this.url = url;
        this.product = product;
        this.basketOperation = basketOperation;
        this.basketCountTextView = basketCountTextView;
        this.imgDecQty = imgDecQty;
        this.imgIncQty = imgIncQty;
        this.btnAddToBasket = btnAddToBasket;
        this.editTextQty = editTextQty;
        this.qty = qty;
    }

    public BasketOperationTask(BaseFragment fragment, String url,
                               BasketOperation basketOperation, @NonNull String productId,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty) {
        this(fragment, url, basketOperation, productId, basketCountTextView, imgDecQty, imgIncQty,
                btnAddToBasket, editTextQty, "1");
    }

    public BasketOperationTask(BaseFragment fragment, String url,
                               BasketOperation basketOperation, @NonNull String productId,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty, String qty) {
        this.fragment = fragment;
        this.url = url;
        this.productId = productId;
        this.basketOperation = basketOperation;
        this.basketCountTextView = basketCountTextView;
        this.imgDecQty = imgDecQty;
        this.imgIncQty = imgIncQty;
        this.btnAddToBasket = btnAddToBasket;
        this.editTextQty = editTextQty;
        this.qty = qty;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        if (isCancelled()) {
            return null;
        }
        String nc = DataUtil.getAddBasketNavigationActivity(fragment.getTag());
        if (fragment.checkInternetConnection()) {
            if (!url.contains(Constants.CART_INC))
                nc = null;
            HashMap<String, String> params = new HashMap<>();
            String reqProdId = product != null ? product.getSku() : productId;
            params.put(Constants.PROD_ID, reqProdId);
            params.put(Constants.QTY, qty);
            params.put(Constants.NC, nc);
            AuthParameters authParameters = AuthParameters.getInstance(fragment.getActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, params, true, authParameters.getBbAuthToken(),
                    authParameters.getVisitorId(), authParameters.getOsVersion(),
                    new BasicCookieStore(), null);
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
                if (basketOperation == BasketOperation.EMPTY) {
                    JsonObject jsonObject = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
                    String status = jsonObject.get(Constants.STATUS).getAsString();
                    if (status.equalsIgnoreCase("OK")) {
                        JsonObject responseJsonObj = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                        CartSummary cartInfo = ParserUtil.parseGetCartSummaryResponse(responseJsonObj);
                        ((CartInfoAware) fragment).setCartInfo(cartInfo);
                        ((CartInfoAware) fragment).updateUIForCartInfo();
                    } else {
                        fragment.showErrorMsg(httpOperationResult.getJsonObject().optString(Constants.BASKET_ERROR_MESSAGE));
                    }
                } else {
                    final BasketOperationResponse basketOperationResponse = ParserUtil.parseBasketOperationResponse(
                            (httpOperationResult.getReponseString()));
                    ((BasketOperationAware) fragment).setBasketOperationResponse(basketOperationResponse);
                    if (basketOperationResponse.getStatus() != null && basketOperationResponse.getStatus()
                            .equalsIgnoreCase("OK")) {   // OK case
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity()).edit();
                        editor.putString("getcart", basketOperationResponse.getNoOfItems());
                        editor.commit();
                        ((BasketOperationAware) fragment).updateUIAfterBasketOperationSuccess(basketOperation,
                                basketCountTextView, imgDecQty, imgIncQty, btnAddToBasket, editTextQty, product, qty);

                    } else if (basketOperationResponse.getStatus() != null && basketOperationResponse.getStatus()
                            .equalsIgnoreCase("ERROR")) {
                        if (basketOperationResponse.getErrorType() != null && basketOperationResponse.getErrorType()
                                .equals(Constants.BASKET_LIMIT_REACHED)) {
                            if (TextUtils.isEmpty(basketOperationResponse.getErrorMessage())) {
                                ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.BASKET_LIMIT_REACHED);
                            } else if (basketOperation == BasketOperation.ADD &&
                                    basketOperationResponse.getErrorType().equals(Constants.BASKET_LIMIT_REACHED)) {
                                ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.BASKET_LIMIT_REACHED);
                            } else {
                                fragment.showErrorMsg(basketOperationResponse.getErrorMessage());
                            }
                            Log.d(TAG, "Sending message: MessageCode.BASKET_LIMIT_REACHED");
                        } else if (basketOperationResponse.getErrorType() != null && basketOperationResponse.getErrorType()
                                .equals(Constants.PRODUCT_ID_NOT_FOUND)) {
                            ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.BASKET_EMPTY);
                            Log.d(TAG, "Sending message: MessageCode.BASKET_EMPTY");
                        }
                        ((BasketOperationAware) fragment).updateUIAfterBasketOperationFailed(basketOperation,
                                basketCountTextView, imgDecQty, imgIncQty, btnAddToBasket, editTextQty, product, null);

                    } else {
                        ((HandlerAware) fragment).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                        Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                    }
                }

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
