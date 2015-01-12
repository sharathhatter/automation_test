package com.bigbasket.mobileapp.model.product.uiv2;

import com.bigbasket.mobileapp.util.Constants;

public enum ProductListType {
    CATEGORY("pc"),
    BRAND("pb"),
    SEARCH("ps"),
    SHOP("sis"),
    BRAND_CATEGORY("pbpc"),
    EXPRESS("express"),
    NOW_AT_BB(Constants.NOW_AT_BB),
    NEW_AT_BB(Constants.NEW_AT_BB),
    BUNDLE_PACK(Constants.BUNDLE_PACK);

    private String value;

    ProductListType(String value) {
        this.value = value;
    }

    public String get() {
        return this.value;
    }
}
