package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartOperationApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.RESPONSE)
    public BasketOperationResponse basketOperationResponse;
}
