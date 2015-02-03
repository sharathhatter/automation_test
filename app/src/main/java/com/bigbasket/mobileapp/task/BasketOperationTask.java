package com.bigbasket.mobileapp.task;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private static final String TAG = BasketOperationTask.class.getName();
    private T context;
    private Product product;
    private BasketOperation basketOperation;
    private String qty;
    private TextView basketCountTextView;
    private View viewIncQty;
    private View viewDecQty;
    private Button btnAddToBasket;
    private EditText editTextQty;
    private String eventName;
    private String sourceName;

    public BasketOperationTask(T context, BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, Button btnAddToBasket,
                               EditText editTextQty, String eventName,
                               String sourceName) {
        this(context, basketOperation, product, basketCountTextView, viewDecQty, viewIncQty,
                btnAddToBasket, editTextQty, "1", eventName, sourceName);
    }

    public BasketOperationTask(T context, BasketOperation basketOperation, @NonNull Product product,
                               TextView basketCountTextView, View viewDecQty,
                               View viewIncQty, Button btnAddToBasket,
                               EditText editTextQty, String qty, String eventName,
                               String sourceName) {
        this.context = context;
        this.product = product;
        this.basketOperation = basketOperation;
        this.basketCountTextView = basketCountTextView;
        this.viewDecQty = viewDecQty;
        this.viewIncQty = viewIncQty;
        this.btnAddToBasket = btnAddToBasket;
        this.editTextQty = editTextQty;
        this.qty = qty;
        this.eventName = eventName;
        this.sourceName = sourceName;
    }

    public void startTask() {
        if (!((ConnectivityAware) context).checkInternetConnection()) {
            ((HandlerAware) context).getHandler().sendOfflineError();
            return;
        }
        createEventTrackPayLoad(eventName, product, sourceName);
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
                    ((BasketOperationAware) context).setBasketOperationResponse(cartOperationApiResponse.basketOperationResponse);
                    ((BasketOperationAware) context).updateUIAfterBasketOperationSuccess(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, btnAddToBasket, editTextQty, product, qty);
                    break;
                case Constants.ERROR:
                    switch (cartOperationApiResponse.errorType) {
                        case Constants.PRODUCT_ID_NOT_FOUND:
                            ((HandlerAware) context).getHandler().
                                    sendEmptyMessage(ApiErrorCodes.BASKET_EMPTY);
                            Log.d(TAG, "Sending message: MessageCode.BASKET_EMPTY");
                            break;
                        default:
                            ((HandlerAware) context).getHandler().sendEmptyMessage(cartOperationApiResponse.getErrorTypeAsInt(),
                                    cartOperationApiResponse.message);
                            Log.d(TAG, "Sending message: ApiErrorCodes.SERVER_ERROR");
                            break;
                    }
                    ((BasketOperationAware) context).updateUIAfterBasketOperationFailed(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, btnAddToBasket, editTextQty, product, null,
                            cartOperationApiResponse.errorType);
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

    private void createEventTrackPayLoad(String eventName, Product product, String sourceName) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.PRODUCT_ID, product.getSku());
        eventAttribs.put(TrackEventkeys.PRODUCT_BRAND, product.getBrand());
        String desc = product.getDescription();
        if (!TextUtils.isEmpty(product.getPackageDescription()))
            desc = " " + product.getWeightAndPackDesc();
        eventAttribs.put(TrackEventkeys.PRODUCT_DESC, desc);
        eventAttribs.put(TrackEventkeys.PRODUCT_TOP_CAT, product.getTopLevelCategoryName());
        eventAttribs.put(TrackEventkeys.PRODUCT_CAT, product.getProductCategoryName());
        ((TrackingAware) context).trackEvent(eventName, eventAttribs, sourceName, null);
    }
}
