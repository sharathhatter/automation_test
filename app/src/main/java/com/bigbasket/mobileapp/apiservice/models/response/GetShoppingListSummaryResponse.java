package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetShoppingListSummaryResponse {

    @SerializedName(Constants.PRODUCT_INFO)
    public ArrayList<ShoppingListSummary> shoppingListSummaries;

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImgUrl;

    @SerializedName(Constants.HEADER_SECTION)
    public Section headerSection;

    @SerializedName(Constants.SHOPPING_LIST_IS_SYSTEM)
    public int isSystem;

    @SerializedName(Constants.HEADER_SEL)
    public int headerSelectedOn;
}
