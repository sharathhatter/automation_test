package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OldBaseApiResponse {
    public String status;
    public String message;

    @SerializedName(Constants.ERROR_TYPE)
    public String errorType;

    public int getErrorTypeAsInt() {
        return Integer.parseInt(errorType);
    }
}
