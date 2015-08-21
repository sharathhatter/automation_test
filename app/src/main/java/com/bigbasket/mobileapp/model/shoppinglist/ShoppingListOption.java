package com.bigbasket.mobileapp.model.shoppinglist;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ShoppingListOption {
    private ShoppingListOption() {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ADD_TO_LIST, DELETE_ITEM, ADD_ALL_ITEM, CREATE})
    public @interface Method{}

    public static final int ADD_TO_LIST = 0;
    public static final int DELETE_ITEM = 1;
    public static final int ADD_ALL_ITEM = 2;
    public static final int CREATE = 3;
}
