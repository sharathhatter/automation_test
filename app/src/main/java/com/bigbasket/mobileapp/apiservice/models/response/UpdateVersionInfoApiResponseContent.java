package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class UpdateVersionInfoApiResponseContent {

    @SerializedName(Constants.MID_KEY)
    public String mId;

    @SerializedName(Constants.USER_DETAILS)
    public LoginUserDetails userDetails;
}
