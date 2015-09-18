package com.bigbasket.mobileapp.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class MemberAddressPageMode {
    private MemberAddressPageMode() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CHECKOUT, ACCOUNT, ADDRESS_SELECT})
    public @interface TYPE {
    }

    public static final int CHECKOUT = 0;
    public static final int ACCOUNT = 1;
    public static final int ADDRESS_SELECT = 2;
}
