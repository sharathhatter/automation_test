package com.bigbasket.mobileapp.model.ads;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by muniraju on 18/12/15.
 */
public class AdAnalyticsData {

    @SerializedName(Constants.CLICKS)
    private int clicks;

    @SerializedName(Constants.IMPRESSIONS)
    private int imps;

    @SerializedName(Constants.CITY_ID)
    private int cityId;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName(Constants.CLIENT)
    private String client = Constants.CLIENT_NAME;

    @SerializedName(Constants.ANALYTICS_ATTRS)
    private Map<String, String> analyticsAttr;

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getImps() {
        return imps;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Map<String, String> getAnalyticsAttr() {
        return analyticsAttr;
    }

    public void setAnalyticsAttr(Map<String, String> analyticsAttr) {
        this.analyticsAttr = analyticsAttr;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
