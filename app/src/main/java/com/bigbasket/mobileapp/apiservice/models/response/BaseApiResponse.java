package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BaseApiResponse {
    private int status;
    private String message;

    @SerializedName(Constants.CART_SUMMARY)
    private CartSummary cartSummary;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public CartSummary getCartSummary() {
        return cartSummary;
    }
}
