package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GetPaymentParamsApiResponseContent {

    @SerializedName(Constants.PAYU_GATEWAY_URL)
    public String payuGatewayUrl;

    @SerializedName(Constants.PAYU_POST_PARAMS)
    public String payuPostParamsJson;

    @SerializedName(Constants.SUCCESS_CAPTURE_URL)
    public String successCaptureUrl;

    @SerializedName(Constants.FAILURE_CAPTURE_URL)
    public String failureCaptureUrl;
}
