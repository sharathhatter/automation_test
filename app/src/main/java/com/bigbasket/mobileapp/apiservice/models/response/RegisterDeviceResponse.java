package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class RegisterDeviceResponse {
    public String status;

    @SerializedName(Constants.VISITOR_ID)
    public String visitorId;
}
