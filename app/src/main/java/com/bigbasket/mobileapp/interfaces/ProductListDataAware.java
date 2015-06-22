package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.product.ProductTabData;

import java.util.HashMap;

public interface ProductListDataAware {
    void setProductTabData(ProductTabData productTabData);

    @Nullable
    HashMap<String, Integer> getCartInfo();

    boolean isNextPageLoading();

    void setNextPageLoading(boolean isNextPageLoading);
}
