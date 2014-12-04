package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartSummaryApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.CART_SUMMARY)
    public CartSummary cartSummary;
}
