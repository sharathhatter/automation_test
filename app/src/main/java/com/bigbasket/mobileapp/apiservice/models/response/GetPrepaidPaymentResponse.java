package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class GetPrepaidPaymentResponse {
    @SerializedName(Constants.POST_PARAMS)
    public HashMap<String, String> postParams;
}
