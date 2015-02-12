package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class LoginUserDetails {

    @SerializedName(Constants.FIRSTNAME)
    public String firstName;

    @SerializedName(Constants.LASTNAME)
    public String lastName;

    @SerializedName(Constants.FULL_NAME)
    public String fullName;

    @SerializedName(Constants.ANALYTICS)
    public LoginAnalyticsDetails analytics;
}
