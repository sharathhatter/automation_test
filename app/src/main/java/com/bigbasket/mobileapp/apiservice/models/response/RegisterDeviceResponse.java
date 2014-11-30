package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class RegisterDeviceResponse {
    private String status;

    @SerializedName(Constants.VISITOR_ID)
    private String visitorId;

    public String getVisitorId() {
        return visitorId;
    }
}
