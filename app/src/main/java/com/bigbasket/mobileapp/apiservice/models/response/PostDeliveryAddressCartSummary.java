package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PostDeliveryAddressCartSummary extends CartSummary {

    @SerializedName(Constants.AMT_PAYABLE)
    public String amtPayable;

    @SerializedName(Constants.WALLET_USED)
    public String walletUsed;

    @SerializedName(Constants.WALLET_REMAINING)
    public String walletRemaining;
}
