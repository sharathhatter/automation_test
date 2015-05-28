package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.order.Order;

/**
 * Created by jugal on 27/5/15.
 */
public interface OrderItemClickAware {

    public void onOrderItemClicked(Order order);
}
