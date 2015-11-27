package com.bigbasket.mobileapp.model.cart;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class BasketOperation {
    private BasketOperation() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INC, DEC, DELETE_ITEM})
    public @interface Mode {
    }

    public static final int INC = 1;
    public static final int DEC = 2;
    public static final int DELETE_ITEM = 3;
}
