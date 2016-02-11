package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class AppDataResponse {

    @SerializedName(Constants.USER_DETAILS)
    public LoginUserDetails userDetails;

    @SerializedName(Constants.CAPABILITIES)
    public AppCapability capabilities;

    @SerializedName(Constants.APP_UPDATE)
    public AppUpdate appUpdate;

    @SerializedName(Constants.TOP_SEARCHES)
    public ArrayList<String> topSearches;

    @SerializedName(Constants.HDFC_POWER_PAY_EXPIRY)
    public int hdfcPayzappExpiry;

    @SerializedName(Constants.CITY_CACHE_EXPIRY)
    public int cityCacheExpiry;

}
