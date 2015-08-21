package com.bigbasket.mobileapp.model.product.uiv2;

import android.support.annotation.StringDef;

import com.bigbasket.mobileapp.util.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ProductListType {
    private ProductListType() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CATEGORY, BRAND, SEARCH, SHOP, BRAND_CATEGORY,
            EXPRESS, NOW_AT_BB, NEW_AT_BB, BUNDLE_PACK})
    public @interface Value {
    }

    public static final String CATEGORY = "pc";
    public static final String BRAND = "pb";
    public static final String SEARCH = "ps";
    public static final String SHOP = "sis";
    public static final String BRAND_CATEGORY = "pbpc";
    public static final String EXPRESS = "express";
    public static final String NOW_AT_BB = Constants.NOW_AT_BB;
    public static final String NEW_AT_BB = Constants.NEW_AT_BB;
    public static final String BUNDLE_PACK = Constants.BUNDLE_PACK;


}
