package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PayNowDetail;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetPayNowParamsResponse extends GetPaymentTypes {
    public String amount;

    @SerializedName(Constants.DETAILS)
    public ArrayList<PayNowDetail> payNowDetailList;
}
