package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListDetail;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GetShoppingListDetailsApiResponse {

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImgUrl;

    @SerializedName(Constants.SHOPPING_LIST_ITEMS)
    public ShoppingListDetail shoppingListDetail;
}
