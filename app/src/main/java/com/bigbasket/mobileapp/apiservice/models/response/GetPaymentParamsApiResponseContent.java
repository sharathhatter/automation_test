package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class GetPaymentParamsApiResponseContent {

    @SerializedName(Constants.PAYU_GATEWAY_URL)
    public String payuGatewayUrl;

    @SerializedName(Constants.POST_PARAMS)
    public JsonObject payuPostParamsJson;

    @SerializedName(Constants.SUCCESS_CAPTURE_URL)
    public String successCaptureUrl;

    @SerializedName(Constants.FAILURE_CAPTURE_URL)
    public String failureCaptureUrl;
}
