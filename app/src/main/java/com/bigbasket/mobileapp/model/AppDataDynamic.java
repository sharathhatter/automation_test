package com.bigbasket.mobileapp.model;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jugal on 4/9/15.
 */
public class AppDataDynamic {

    @SerializedName(Constants.ADD_TO_BASKET_POST_PARAMS)
    private ArrayList<String> addBasketPostParams;

    @SerializedName(Constants.EXPRESS_AVAILABLE)
    private String expressAvailable;

    @SerializedName(Constants.AVAILABILITY_INFOS)
    private HashMap<String, String> avalabilityInfos;
}
