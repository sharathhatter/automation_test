package com.bigbasket.mobileapp.task;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.ShowCartActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.CartOperationApiResponse;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListProductFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.OnBasketChangeListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import retrofit.Call;

public class BasketOperationTask<T extends AppOperationAware> {

    private T context;
    private Product product;
    private
    @BasketOperation.Mode
    int basketOperation;
    private String qty;
    private WeakReference<TextView> basketCountTextView;
    private WeakReference<View> viewIncQty;
    private WeakReference<View> viewDecQty;
    private WeakReference<View> viewAddToBasket;
    private String eventName;
    private WeakReference<View> productView;
    private String navigationCtx;
    private WeakReference<HashMap<String, Integer>> cartInfo;
    private WeakReference<EditText> editTextQty;
    private String tabName;
    private WeakReference<Map<String, String>> basketQueryMap;

    /**
     * @param context             This activity on behalf of which this request is being made
     * @param basketOperation     Type of basket operation. eg. SET, DEC, INC or DELETE_ITEM
     * @param product             Product object for which quantity has to be changed
     * @param basketCountTextView Basket quantity text-view that should be updated once the operation completes
     * @param viewDecQty          Basket qty increment widget that should be toggled on basket operation completion
     * @param viewIncQty          Basket qty decrement widget that should be toggled on basket operation completion
     * @param viewAddToBasket     Add to basket button whose visibility should get toggle on basket operation completion
     * @param eventName           Event-name to identify the screen from the item is being added/removed
     * @param navigationCtx       Parent screen of the widget from where the user arrived on this page
     * @param productView         Base Product row that should be updated to handle the basket change
     * @param cartInfo            Product-ID & Cart quantity map that should be updated in order to update other tabs where this product may occur.
     * @param editTextQty         Quantity input view whose visibility should get toggle on basket operation completion. This view will be
     *                            only shown to Kirana users
     * @param tabName             Tab-name under which the product currently resides
     * @param basketQueryMap      Additional details like store-id, availability info-id that should be send along-with basket operations
     * @param qty                 Quantity of the product to be added/removed. This field is passed when a Kirana member
     *                            is adding product quantity via the input-view
     */
    private BasketOperationTask(T context, @BasketOperation.Mode int basketOperation, @NonNull Product product,
                                WeakReference<TextView> basketCountTextView, WeakReference<View> viewDecQty,
                                WeakReference<View> viewIncQty, WeakReference<View> viewAddToBasket,
                                String qty, String eventName,
                                String navigationCtx, WeakReference<View> productView,
                                WeakReference<HashMap<String, Integer>> cartInfo,
                                WeakReference<EditText> editTextQty,
                                String tabName, WeakReference<Map<String, String>> basketQueryMap) {
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
        this.basketQueryMap = basketQueryMap;
    }

    /**
     * Trigger network request to update the basket
     */
    public void startTask() {
        if (!context.checkInternetConnection()) {
            context.getHandler().sendOfflineError();
            return;
        }
        logBasketEvent(eventName, product, navigationCtx);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(context.getCurrentActivity());
        context.showProgressDialog(context.getCurrentActivity()
                .getString(R.string.please_wait));
        String reqProdId = product.getSku();
        Call<CartOperationApiResponse> call = null;
        String searchTerm = null;
        if (navigationCtx != null && (navigationCtx.startsWith("pl.ps"))) {
            navigationCtx = "pl.ps";
            String[] searchTermArray = navigationCtx.split("\\.");
            if (searchTermArray.length == 3) {
                searchTerm = searchTermArray[2];
            }
        }
        switch (basketOperation) {
            case BasketOperation.INC:
                call = bigBasketApiService.incrementCartItem(navigationCtx, searchTerm, reqProdId, qty,
                        basketQueryMap != null ? basketQueryMap.get() : null);
                break;
            case BasketOperation.DEC:
                call = bigBasketApiService.decrementCartItem(navigationCtx, reqProdId, qty,
                        basketQueryMap != null ? basketQueryMap.get() : null);
                break;
            case BasketOperation.DELETE_ITEM:
                call = bigBasketApiService.setCartItem(navigationCtx, null, reqProdId, "0",
                        basketQueryMap != null ? basketQueryMap.get() : null);
                break;
        }
        if (call != null) {
            call.enqueue(new CartOperationApiResponseCallback(context));
        }
    }

    /**
     * Log basket operation event to all the analytics provider
     */
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

        if (context instanceof ShoppingListProductFragment) {
            navigationCtx = ((ShoppingListSummaryActivity) context.getCurrentActivity()).getCurrentNavigationContextForSl();
        }

        if (navigationCtx != null) {
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        }

        eventAttribs.put(TrackEventkeys.TAB_NAME, tabName);

        ((TrackingAware) context).trackEvent(eventName, eventAttribs, navigationCtx, null, false, true);
        if (!TextUtils.isEmpty(eventName) && eventName.equals(TrackingAware.BASKET_ADD)) {
            ((TrackingAware) context).trackEventAppsFlyer(eventName);
        }
    }

    /**
     * Builder class to create a basket add/remove operation
     */
    public static class Builder<T extends AppOperationAware> {
        private T context;
        private Product product;
        private
        @BasketOperation.Mode
        int basketOperation;
        private String qty;
        private WeakReference<TextView> basketCountTextView;
        private WeakReference<View> viewIncQty;
        private WeakReference<View> viewDecQty;
        private WeakReference<View> viewAddToBasket;
        private String eventName;
        private WeakReference<View> productView;
        private String navigationCtx;
        private WeakReference<HashMap<String, Integer>> cartInfo;
        private WeakReference<EditText> editTextQty;
        private String tabName;
        private WeakReference<Map<String, String>> basketQueryMap;

        public Builder(T context,
                       @BasketOperation.Mode int basketOperation,
                       @NonNull Product product) {
            this.context = context;
            this.basketOperation = basketOperation;
            this.product = product;
        }

        public Builder withQty(String qty) {
            this.qty = qty;
            return this;
        }

        public Builder withEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public Builder withNavigationCtx(String navigationCtx) {
            this.navigationCtx = navigationCtx;
            return this;
        }

        public Builder withTabName(String tabName) {
            this.tabName = tabName;
            return this;
        }

        public Builder withBasketCountTextView(TextView basketCountTextView) {
            this.basketCountTextView = new WeakReference<>(basketCountTextView);
            return this;
        }

        public Builder withViewIncQty(View viewIncQty) {
            this.viewIncQty = new WeakReference<>(viewIncQty);
            return this;
        }

        public Builder withViewDecQty(View viewDecQty) {
            this.viewDecQty = new WeakReference<>(viewDecQty);
            return this;
        }

        public Builder withViewAddToBasket(View viewAddToBasket) {
            this.viewAddToBasket = new WeakReference<>(viewAddToBasket);
            return this;
        }

        public Builder withProductView(View productView) {
            this.productView = new WeakReference<>(productView);
            return this;
        }

        public Builder withCartInfo(@Nullable HashMap<String, Integer> cartInfo) {
            this.cartInfo = new WeakReference<>(cartInfo);
            return this;
        }

        public Builder withEditTextQty(@Nullable EditText editTextQty) {
            this.editTextQty = new WeakReference<>(editTextQty);
            return this;
        }

        public Builder withBasketQueryMap(@Nullable Map<String, String> basketQueryMap) {
            this.basketQueryMap = new WeakReference<>(basketQueryMap);
            return this;
        }

        public BasketOperationTask build() {
            return new BasketOperationTask<>(context, basketOperation, product, basketCountTextView,
                    viewDecQty, viewIncQty, viewAddToBasket, qty, eventName, navigationCtx,
                    productView, cartInfo, editTextQty, tabName, basketQueryMap);
        }
    }

    /**
     * Callback class that implements the Retrofit's response
     */
    private class CartOperationApiResponseCallback extends BBNetworkCallback<CartOperationApiResponse> {
        public CartOperationApiResponseCallback(AppOperationAware ctx) {
            super(ctx);
        }

        @Override
        public void onSuccess(CartOperationApiResponse cartOperationApiResponse) {
            switch (cartOperationApiResponse.status) {
                case Constants.OK:
                    // Update the product view and also update the FAB basket button
                    ((CartInfoAware) context).setCartSummary(cartOperationApiResponse.basketOperationResponse.getCartSummary());
                    ((CartInfoAware) context).updateUIForCartInfo();
                    ((CartInfoAware) context).markBasketDirty();
                    if (context instanceof OnBasketChangeListener && (context instanceof ShowCartActivity ||
                            context instanceof ProductDetailFragment)) {
                        Intent data = new Intent();
                        data.putExtra(Constants.SKU_ID, product.getSku());
                        data.putExtra(Constants.PRODUCT_NO_ITEM_IN_CART, product.getNoOfItemsInCart());
                        ((OnBasketChangeListener) context).markBasketChanged(data);
                    }
                    ((BasketOperationAware) context).setBasketOperationResponse(cartOperationApiResponse.basketOperationResponse);
                    ((BasketOperationAware) context).updateUIAfterBasketOperationSuccess(basketOperation,
                            basketCountTextView, viewDecQty, viewIncQty, viewAddToBasket, product, qty,
                            productView, cartInfo, editTextQty);
                    break;
                case Constants.ERROR:
                    switch (cartOperationApiResponse.errorType) {
                        case Constants.PRODUCT_ID_NOT_FOUND:
                            context.getHandler().
                                    sendEmptyMessage(ApiErrorCodes.BASKET_EMPTY, null);
                            break;
                        default:
                            // Pass to generic error handler
                            context.getHandler().sendEmptyMessage(cartOperationApiResponse.getErrorTypeAsInt(),
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
        public boolean updateProgress() {
            context.hideProgressDialog();
            return true;
        }
    }


}
