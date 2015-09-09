package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartGetApiCartItemsContent {

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImgUrl;

    @SerializedName(Constants.ITEMS)
    public ArrayList<CartItemList> cartItemLists;

}
