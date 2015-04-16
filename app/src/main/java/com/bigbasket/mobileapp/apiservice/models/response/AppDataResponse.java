package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class AppDataResponse {

    @SerializedName(Constants.ANALYTICS)
    public LoginUserDetails userDetails;

    @SerializedName(Constants.CAPABILITIES)
    public AnalyticsEngine capabilities;

    @SerializedName(Constants.APP_UPDATE)
    public AppUpdate appUpdate;

    @SerializedName(Constants.TOP_SEARCHES)
    public ArrayList<String> topSearches;


}
