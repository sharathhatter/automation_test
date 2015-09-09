package com.bigbasket.mobileapp.task;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.CartOperationApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.OnBasketChangeListener;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BasketOperationTask<T> {

    private T context;
    private Product product;
    private
    @BasketOperation.Mode
    int basketOperation;
    private String qty;
    private TextView basketCountTextView;
    private View viewIncQty;
    private View viewDecQty;
    private View viewAddToBasket;
    private String eventName;
    private View productView;
    private String navigationCtx;
    @Nullable
    private HashMap<String, Integer> cartInfo;
    @Nullable
    private EditText editTextQty;
    private String tabName;

    public BasketOperationTask(T context, @BasketOperation.Mode int basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, View viewAddToBasket, String eventName,
                               String navigationCtx, @Nullable View productView,
                               @Nullable HashMap<String, Integer> cartInfo,
                               @Nullable EditText editTextQty,
                               String tabName) {
        this(context, basketOperation, product, basketCountTextView, viewDecQty, viewIncQty,
                viewAddToBasket, "1", eventName, navigationCtx, productView, cartInfo, editTextQty,
                tabName);
    }

    public BasketOperationTask(T context, @BasketOperation.Mode int basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, View viewAddToBasket,
                               String qty, String eventName,
                               String navigationCtx, @Nullable View productView,
                               @Nullable HashMap<String, Integer> cartInfo,
                               @Nullable EditText editTextQty,
                               String tabName) {
        this.context = context;
        this.product = product;
        this.basketOperation = basketOperation;
        this.basketCountTextView = basketCountTextView;
        this.viewDecQty = viewDecQty;
        this.viewIncQty = viewIncQty;
        this.viewAddToBasket = viewAddToBasket;
        this.qty = qty;
        this.eventName = eventName;
        this.productView = productView;
        this.navigationCtx = navigationCtx;
        this.cartInfo = cartInfo;
        this.editTextQty = editTextQty;
        this.tabName = tabName;
    }

    public void startTask() {
        if (!((ConnectivityAware) context).checkInternetConnection()) {
            ((HandlerAware) context).getHandler().sendOfflineError();
            return;
        }
        logBasketEvent(eventName, product, navigationCtx);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) context).getCurrentActivity());
        ((ProgressIndicationAware) context).showProgressDialog(((ActivityAware) context).getCurrentActivity()
                .getString(R.string.please_wait));
        String reqProdId = product.getSku();
        switch (basketOperation) {
            case BasketOperation.INC:
                bigBasketApiService.incrementCartItem(navigationCtx, reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case BasketOperation.DEC:
                bigBasketApiService.decrementCartItem(navigationCtx, reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case BasketOperation.SET:
                bigBasketApiService.setCartItem(navigationCtx, reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case BasketOperation.EMPTY:
                bigBasketApiService.setCartItem(navigationCtx, reqProdId, "0", new CartOperationApiResponseCallback());
                break;
        }
    }

    private void logBasketEvent(String eventName, Product product, String navigationCtx) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.PRODUCT_ID, product.getSku());
        eventAttribs.put(TrackEventkeys.PRODUCT_BRAND, product.getBrand());
        String desc = product.getDescription();
        if (!TextUtils.isEmpty(product.getPackageDescription()))
            desc = " " + product.getWeightAndPackDesc();
        eventAttribs.put(TrackEventkeys.PRODUCT_DESC, desc);
        eventAttribs.put(TrackEventkeys.PRODUCT_TOP_CAT, product.getTopLevelCategoryName());
        eventAttribs.put(TrackEventkeys.PRODUCT_CAT, product.getProductCategoryName());
        if (navigationCtx != null) {
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        }
        eventAttribs.put(TrackEventkeys.TAB_NAME, tabName);
        ((TrackingAware) context).trackEvent(eventName, eventAttribs, navigationCtx, null, false, true);
        if (!TextUtils.isEmpty(eventName) && eventName.equals(TrackingAware.BASKET_ADD)) {
            ((TrackingAware) context).trackEventAppsFlyer(eventName);
        }
    }

    private class CartOperationApiResponseCallback implements Callback<CartOperationApiResponse> {

        @Override
        public void success(CartOperationApiResponse cartOperationApiResponse, Response response) {
            if (((CancelableAware) context).isSuspended()) {
                return;
            } else {
                try {
                    ((ProgressIndicationAware) context).hideProgressDialog();
                } catch (IllegalArgumentException ex) {
                    return;
                }
            }
            switch (cartOperationApiResponse.status) {
                case Constants.OK:
                    ((CartInfoAware) context).setCartSummary(cartOperationApiResponse.basketOperationResponse.getCartSummary());
                    ((CartInfoAware) context).updateUIForCartInfo();
                    ((CartInfoAware) context).markBasketDirty();
                    if (!TextUtils.isEmpty(navigationCtx) && navigationCtx.equals(TrackEventkeys.NAVIGATION_CTX_SHOW_BASKET)
                            && context instanceof OnBasketChangeListener) {
                        ((OnBasketChangeListener) context).markBasketChanged(null);
                    }
                    ((BasketOperationAware) context).setBasketOperationResponse(cartOperationApiResponse.basketOperationResponse);
                    Log.d("CONTEXT CHECK", ""+context.getClass().getSimpleName());
                    ((BasketOperationAware) context).updateUIAfterBasketOperationSuccess(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, viewAddToBasket, product, qty,
                            productView, cartInfo, editTextQty);
                    break;
                case Constants.ERROR:
                    switch (cartOperationApiResponse.errorType) {
                        case Constants.PRODUCT_ID_NOT_FOUND:
                            ((HandlerAware) context).getHandler().
                                    sendEmptyMessage(ApiErrorCodes.BASKET_EMPTY, null);
                            break;
                        default:
                            ((HandlerAware) context).getHandler().sendEmptyMessage(cartOperationApiResponse.getErrorTypeAsInt(),
                                    cartOperationApiResponse.message);
                            break;
                    }
                    ((BasketOperationAware) context).updateUIAfterBasketOperationFailed(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, viewAddToBasket, product, null,
                            cartOperationApiResponse.errorType, productView, editTextQty);
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (((CancelableAware) context).isSuspended()) {
                return;
            } else {
                try {
                    ((ProgressIndicationAware) context).hideProgressDialog();
                } catch (IllegalArgumentException ex) {
                    return;
                }
            }
            ((HandlerAware) context).getHandler().handleRetrofitError(error);
        }
    }
}
