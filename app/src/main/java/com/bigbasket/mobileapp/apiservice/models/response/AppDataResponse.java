package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 2/2/15.
 */
public class AppDataResponse {

    @SerializedName(Constants.ANALYTICS)
    public LoginUserDetails userDetails;

    @SerializedName(Constants.CAPABILITIES)
    public AnalyticsEngine capabilities;

    @SerializedName(Constants.APP_UPDATE)
    public AppUpdate appUpdate;

}
