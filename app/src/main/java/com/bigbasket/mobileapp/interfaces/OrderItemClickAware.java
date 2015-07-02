package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.order.Order;

/**
 * Created by jugal on 27/5/15.
 */
public interface OrderItemClickAware {

    void onOrderItemClicked(Order order);
}
