package com.bigbasket.mobileapp.adapter.product;

/**
 * Created by muniraju on 16/12/15.
 */
public abstract class AbstractProductItem {
    private final int type;

    public AbstractProductItem(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
