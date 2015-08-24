package com.bigbasket.mobileapp.model.cart;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class BasketOperation {
    private BasketOperation() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SET, INC, DEC, EMPTY})
    public @interface Mode {
    }

    public static final int SET = 0;
    public static final int INC = 1;
    public static final int DEC = 2;
    public static final int EMPTY = 3;
}
