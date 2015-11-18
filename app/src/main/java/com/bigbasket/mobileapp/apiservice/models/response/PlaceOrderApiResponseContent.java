package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PlaceOrderApiResponseContent {

    @SerializedName(Constants.ORDERS)
    public ArrayList<Order> orders;

    @SerializedName(Constants.ADD_MORE_LINK)
    public String addMoreLink;

    @SerializedName(Constants.ADD_MORE_MSG)
    public String addMoreMsg;

}
