package com.bigbasket.mobileapp.model.product.uiv2;

public enum ProductListType {
    CATEGORY("pc"),
    BRAND("pb"),
    SEARCH("ps"),
    SHOP("sis"),
    BRAND_CATEGORY("pbpc"),
    EXPRESS("express");

    private String value;

    ProductListType(String value) {
        this.value = value;
    }

    public String get() {
        return this.value;
    }
}
