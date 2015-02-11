package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BrowsePromoCategoryApiResponseContent extends GetDynamicPageApiResponse {

    @SerializedName(Constants.PROMO_CATS)
    public ArrayList<PromoCategory> promoCategories;
}
