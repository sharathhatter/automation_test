package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class LoginApiResponse {

    @SerializedName(Constants.BB_TOKEN)
    public String bbToken;

    @SerializedName(Constants.MID_KEY)
    public String mId;

    public String email;

    @SerializedName(Constants.USER_DETAILS)
    public LoginUserDetails userDetails;
}
