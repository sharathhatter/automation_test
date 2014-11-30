package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BaseApiResponse {
    public int status;
    public String message;

    @SerializedName(Constants.CART_SUMMARY)
    public CartSummary cartSummary;
}
