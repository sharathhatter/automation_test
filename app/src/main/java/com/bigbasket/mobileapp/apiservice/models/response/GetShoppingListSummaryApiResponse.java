package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetShoppingListSummaryApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.SHOPPING_LIST_SUMMARY)
    public ArrayList<ShoppingListSummary> shoppingListSummaries;
}
