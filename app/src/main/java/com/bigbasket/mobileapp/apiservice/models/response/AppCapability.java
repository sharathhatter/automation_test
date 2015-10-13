package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class AppCapability {

    @SerializedName(Constants.ENABLE_MOENGAGE)
    private boolean isMoEngageEnabled;
    @SerializedName(Constants.ENABLE_LOCALYTICS)
    private boolean isAnalyticsEnabled;
    @SerializedName(Constants.ENABLE_FB_LOGGER)
    private boolean isFBLoggerEnabled;
    @SerializedName(Constants.IS_MULTICITY_ENABLED)
    private boolean isMultiCityEnabled;

    public boolean isMoEngageEnabled() {
        return isMoEngageEnabled;
    }

    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }


    public boolean isFBLoggerEnabled() {
        return isFBLoggerEnabled;
    }

    public boolean isMultiCityEnabled() {
        return isMultiCityEnabled;
    }
}
