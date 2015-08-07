package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PowerPayPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GetPowerPayPaymentParamsResponse {
    @SerializedName(Constants.POST_PARAMS)
    public PowerPayPostParams powerPayPostParams;
}
