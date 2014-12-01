package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.promo.PromoMessage;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PromoSummaryApiResponseContent {

    public double saving;

    @SerializedName(Constants.NUM_IN_BASKET)
    public int numInBasket;

    @SerializedName(Constants.INFO_MESSAGE)
    public PromoMessage promoMessage;
}
