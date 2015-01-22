package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class LoginAnalyticsDetails {
    @SerializedName(Constants.GENDER)
    public String gender;

    @SerializedName(Constants.CREATED_ON)
    public String createdOn;

    @SerializedName(Constants.DOB)
    public String dateOfBirth;

    @SerializedName(Constants.MOBILE_NUMBER)
    public String mobileNumber;

    @SerializedName(Constants.HUB)
    public String hub;

    public String city;

    @SerializedName(Constants.CITY_ID)
    public int cityId;

    @SerializedName(Constants.ANALYTICS_ADDITIONAL_ATTRS)
    public HashMap<String, Object> additionalAttrs;
}
