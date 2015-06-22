package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class PromoDetailApiResponseContent {

    @SerializedName(Constants.PROMO_DETAILS)
    public PromoDetail promoDetail;

    @SerializedName(Constants.CART_INFO)
    public HashMap<String, Integer> cartInfo;

}
