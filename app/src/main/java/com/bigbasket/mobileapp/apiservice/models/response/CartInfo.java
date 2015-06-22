package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by jugal on 19/6/15.
 */
public class CartInfo {

    @SerializedName(Constants.CART_INFO)
    public HashMap<String, Integer> cartInfo;
}
