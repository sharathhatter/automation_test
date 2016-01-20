package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PayzappPrePaymentParamsResponse {
    @SerializedName(Constants.POST_PARAMS)
    public PayzappPostParams payzappPostParams;
    @SerializedName(Constants.TXT_ORDER_ID)
    public String txnOrderId;
}
