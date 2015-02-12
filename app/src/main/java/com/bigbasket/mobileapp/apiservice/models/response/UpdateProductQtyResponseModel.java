package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 4/2/15.
 */
public class UpdateProductQtyResponseModel {

    @SerializedName(Constants.POTENTIAL_ORDER_ID)
    public String potentialOrderId;

    @SerializedName(Constants.CART_SUMMARY)
    public CartSummary cartSummary;
}
