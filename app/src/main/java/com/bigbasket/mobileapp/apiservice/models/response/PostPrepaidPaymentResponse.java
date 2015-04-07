package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PostPrepaidPaymentResponse {
    @SerializedName(Constants.PAYMENT_STATUS)
    public boolean paymentStatus;
}
