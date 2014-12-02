package com.bigbasket.mobileapp.model.cart;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BasketOperationResponse {

    @SerializedName(Constants.CART_SUMMARY)
    private CartSummary cartSummary;

    @SerializedName(Constants.BASKET_CART_INNER)
    private BasketResponseProductInfo basketResponseProductInfo;

    public CartSummary getCartSummary() {
        return cartSummary;
    }

    public BasketResponseProductInfo getBasketResponseProductInfo() {
        return basketResponseProductInfo;
    }
}
