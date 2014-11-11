package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.order.OrderSummary;

public interface PlaceOrderAware {
    public void onPlaceOrderAction(OrderSummary orderSummary);
}
