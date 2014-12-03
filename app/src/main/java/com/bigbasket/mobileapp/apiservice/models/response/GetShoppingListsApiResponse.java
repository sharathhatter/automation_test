package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetShoppingListsApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.SHOPPING_LISTS)
    public ArrayList<ShoppingListName> shoppingListNames;
}
