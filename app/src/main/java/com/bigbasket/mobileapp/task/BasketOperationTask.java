package com.bigbasket.mobileapp.task;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class BasketOperationTask<T> extends AsyncTask<String, Long, Void> {

    private static final String TAG = BasketOperationTask.class.getName();
    private T context;
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

    public BasketOperationTask(T context, String url,
                               BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty) {
        this(context, url, basketOperation, product, basketCountTextView, imgDecQty, imgIncQty,
                btnAddToBasket, editTextQty, "1");
    }

    public BasketOperationTask(T context, String url,
                               BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty, String qty) {
        this.context = context;
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

    public BasketOperationTask(T context, String url,
                               BasketOperation basketOperation, @NonNull String productId,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty) {
        this(context, url, basketOperation, productId, basketCountTextView, imgDecQty, imgIncQty,
                btnAddToBasket, editTextQty, "1");
    }

    public BasketOperationTask(T context, String url,
                               BasketOperation basketOperation, @NonNull String productId,
                               TextView basketCountTextView, ImageView imgDecQty,
                               ImageView imgIncQty, Button btnAddToBasket,
                               EditText editTextQty, String qty) {
        this.context = context;
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
        String nc = ""; //DataUtil.getAddBasketNavigationActivity(fragment.getTag()); TODO : Fix this
        if (((ConnectivityAware) context).checkInternetConnection()) {
            if (!url.contains(Constants.CART_INC))
                nc = null;
            HashMap<String, String> params = new HashMap<>();
            String reqProdId = product != null ? product.getSku() : productId;
            params.put(Constants.PROD_ID, reqProdId);
            params.put(Constants.QTY, qty);
            params.put(Constants.NC, nc);
            AuthParameters authParameters = AuthParameters.getInstance(((ActivityAware) context).getCurrentActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, params, true, authParameters.getBbAuthToken(),
                    authParameters.getVisitorId(), authParameters.getOsVersion(),
                    new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
        } else {
            if (context instanceof Fragment) {
                ((BaseFragment) context).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            } else {
                ((BaseActivity) context).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            }
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");

        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (((CancelableAware) context).isSuspended()) {
            cancel(true);
        } else {
            if (context instanceof BaseFragment) {
                ((BaseFragment) context).showProgressDialog(((BaseFragment) context).getString(R.string.please_wait));
            } else {
                ((BaseActivity) context).showProgressDialog(((BaseActivity) context).getString(R.string.please_wait));
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (((CancelableAware) context).isSuspended()) {
            return;
        } else {
            try {
                if (context instanceof BaseFragment) {
                    ((BaseFragment) context).hideProgressDialog();
                } else {
                    ((BaseActivity) context).hideProgressDialog();
                }
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
                        ((CartInfoAware) context).setCartInfo(cartInfo);
                        ((CartInfoAware) context).updateUIForCartInfo();
                    } else {
                        if (context instanceof BaseFragment) {
                            ((BaseFragment) context).showErrorMsg(httpOperationResult.getJsonObject().optString(Constants.BASKET_ERROR_MESSAGE));
                        } else {
                            ((BaseActivity) context).showAlertDialog((BaseActivity) context, null, httpOperationResult.getJsonObject().optString(Constants.BASKET_ERROR_MESSAGE));
                        }
                    }
                } else {
                    final BasketOperationResponse basketOperationResponse = ParserUtil.parseBasketOperationResponse(
                            (httpOperationResult.getReponseString()));
                    ((BasketOperationAware) context).setBasketOperationResponse(basketOperationResponse);
                    if (basketOperationResponse.getStatus() != null && basketOperationResponse.getStatus()
                            .equalsIgnoreCase("OK")) {   // OK case
                        Context ctx = context instanceof Fragment ? ((Fragment) context).getActivity() : (Activity) context;
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
                        editor.putString("getcart", basketOperationResponse.getNoOfItems());
                        editor.commit();
                        ((BasketOperationAware) context).updateUIAfterBasketOperationSuccess(basketOperation,
                                basketCountTextView, imgDecQty, imgIncQty, btnAddToBasket, editTextQty, product, qty);

                    } else if (basketOperationResponse.getStatus() != null && basketOperationResponse.getStatus()
                            .equalsIgnoreCase("ERROR")) {
                        if (basketOperationResponse.getErrorType() != null && basketOperationResponse.getErrorType()
                                .equals(Constants.BASKET_LIMIT_REACHED)) {
                            if (TextUtils.isEmpty(basketOperationResponse.getErrorMessage())) {
                                ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.BASKET_LIMIT_REACHED);
                            } else if (basketOperation == BasketOperation.ADD &&
                                    basketOperationResponse.getErrorType().equals(Constants.BASKET_LIMIT_REACHED)) {
                                ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.BASKET_LIMIT_REACHED);
                            } else {
                                if (context instanceof BaseFragment) {
                                    ((BaseFragment) context).showErrorMsg(basketOperationResponse.getErrorMessage());
                                } else {
                                    ((BaseActivity) context).showAlertDialog((BaseActivity) context, null, basketOperationResponse.getErrorMessage());
                                }
                            }
                            Log.d(TAG, "Sending message: MessageCode.BASKET_LIMIT_REACHED");
                        } else if (basketOperationResponse.getErrorType() != null && basketOperationResponse.getErrorType()
                                .equals(Constants.PRODUCT_ID_NOT_FOUND)) {
                            ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.BASKET_EMPTY);
                            Log.d(TAG, "Sending message: MessageCode.BASKET_EMPTY");
                        }
                        ((BasketOperationAware) context).updateUIAfterBasketOperationFailed(basketOperation,
                                basketCountTextView, imgDecQty, imgIncQty, btnAddToBasket, editTextQty, product, null);

                    } else {
                        ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                        Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                    }
                }

            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) context).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }

        super.onPostExecute(result);
    }
}
