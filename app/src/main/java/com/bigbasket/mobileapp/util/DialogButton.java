package com.bigbasket.mobileapp.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class DialogButton {
    private DialogButton() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OK, YES, NO, CANCEL, NONE})
    public @interface ButtonType {
    }

    public static final int OK = 0;
    public static final int YES = 1;
    public static final int NO = 2;
    public static final int CANCEL = 3;
    public static final int NONE = 4;
}
