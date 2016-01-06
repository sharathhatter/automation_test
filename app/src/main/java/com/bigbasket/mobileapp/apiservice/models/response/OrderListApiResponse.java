package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderListApiResponse {

    @SerializedName(Constants.ORDERS)
    public ArrayList<Order> orders;

    @SerializedName(Constants.TOTAL_PAGE)
    public int totalPages;

}
