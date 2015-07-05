package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;


public class AddAllShoppingListItemResponse extends OldApiResponseWithCart {

    @SerializedName(Constants.CART_INFO)
    public HashMap<String, Integer> cartInfo;
}
