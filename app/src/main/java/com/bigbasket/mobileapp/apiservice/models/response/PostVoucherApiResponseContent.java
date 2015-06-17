package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostVoucherApiResponseContent {

    @SerializedName(Constants.CREDIT_DETAILS)
    public ArrayList<CreditDetails> creditDetails;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderDetails orderDetails;
}
