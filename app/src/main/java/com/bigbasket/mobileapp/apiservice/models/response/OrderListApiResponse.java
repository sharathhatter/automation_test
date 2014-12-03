package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderMonthRange;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderListApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.RESPONSE)
    public ArrayList<Order> orders;

    @SerializedName(Constants.ORDER_MONTH_RANGE)
    public ArrayList<OrderMonthRange> orderMonthRanges;

    @SerializedName(Constants.MONTHS_DATA)
    public int selectedMonth;
}
