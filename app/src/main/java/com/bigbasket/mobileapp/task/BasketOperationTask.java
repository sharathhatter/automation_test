package com.bigbasket.mobileapp.task;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
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
    private BasketOperation basketOperation;
    private String qty;
    private TextView basketCountTextView;
    private View viewIncQty;
    private View viewDecQty;
    private View viewAddToBasket;
    private String eventName;
    private View productView;
    private String navigationCtx;

    public BasketOperationTask(T context, BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, View viewAddToBasket, String eventName,
                               String navigationCtx, @Nullable View productView) {
        this(context, basketOperation, product, basketCountTextView, viewDecQty, viewIncQty,
                viewAddToBasket, "1", eventName, navigationCtx, productView);
    }

    public BasketOperationTask(T context, BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, View viewAddToBasket,
                               String qty, String eventName,
                               String navigationCtx, @Nullable View productView) {
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
            case INC:
                bigBasketApiService.incrementCartItem(reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case DEC:
                bigBasketApiService.decrementCartItem(reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case SET:
                bigBasketApiService.setCartItem(reqProdId, qty, new CartOperationApiResponseCallback());
                break;
            case EMPTY:
                bigBasketApiService.setCartItem(reqProdId, "0", new CartOperationApiResponseCallback());
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
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        ((TrackingAware) context).trackEvent(eventName, eventAttribs, navigationCtx, null, false);
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
                    ((CartInfoAware) context).setCartInfo(cartOperationApiResponse.basketOperationResponse.getCartSummary());
                    ((CartInfoAware) context).updateUIForCartInfo();
                    ((CartInfoAware) context).markBasketDirty();
                    ((BasketOperationAware) context).setBasketOperationResponse(cartOperationApiResponse.basketOperationResponse);
                    ((BasketOperationAware) context).updateUIAfterBasketOperationSuccess(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, viewAddToBasket, product, qty,
                            productView);
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
                            cartOperationApiResponse.errorType, productView);
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
