package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class LoginApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.VISITOR_ID)
    public String visitorId;

    @SerializedName(Constants.BB_TOKEN)
    public String bbToken;

    @SerializedName(Constants.MID_KEY)
    public String mId;

    @SerializedName(Constants.USER_DETAILS)
    public LoginUserDetails userDetails;
}
