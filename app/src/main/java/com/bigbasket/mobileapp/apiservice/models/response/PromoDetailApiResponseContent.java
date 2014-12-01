package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PromoDetailApiResponseContent {

    @SerializedName(Constants.PROMO_DETAILS)
    public PromoDetail promoDetail;
}
