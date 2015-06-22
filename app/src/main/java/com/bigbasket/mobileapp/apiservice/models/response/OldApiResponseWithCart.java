package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 18/6/15.
 */
public class OldApiResponseWithCart extends CartSummary {
    public String status;
    public String message;

    @SerializedName(Constants.ERROR_TYPE)
    public String errorType;

    public int getErrorTypeAsInt() {
        return Integer.parseInt(errorType);
    }
}
