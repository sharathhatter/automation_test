package com.bigbasket.mobileapp.interfaces.payment;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.order.Order;

import java.util.ArrayList;

public interface OnPaymentValidationListener {
    void onPaymentValidated(boolean status, @Nullable String msg,
                            @Nullable ArrayList<Order> orders);
}
