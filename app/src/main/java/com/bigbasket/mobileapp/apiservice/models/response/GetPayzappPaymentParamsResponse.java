package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GetPayzappPaymentParamsResponse {
    @SerializedName(Constants.POST_PARAMS)
    public PayzappPostParams payzappPostParams;
}
