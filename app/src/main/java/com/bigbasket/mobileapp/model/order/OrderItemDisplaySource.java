package com.bigbasket.mobileapp.model.order;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class OrderItemDisplaySource {
    private OrderItemDisplaySource() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BASKET, ORDER_DISPLAY})
    public @interface Type {
    }

    public static final int BASKET = 0;
    public static final int ORDER_DISPLAY = 1;
}
