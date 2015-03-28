package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetShoppingListSummaryResponse {

    @SerializedName(Constants.PRODUCT_INFO)
    public ArrayList<ShoppingListSummary> shoppingListSummaries;

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImgUrl;
}
