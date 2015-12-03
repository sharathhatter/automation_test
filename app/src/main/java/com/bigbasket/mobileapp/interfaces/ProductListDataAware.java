package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.product.ProductTabData;

import java.util.HashMap;

public interface ProductListDataAware {
    void setProductTabData(ProductTabData productTabData, boolean isFilterOrSortApplied,
                           int currentTabIndx);

    @Nullable
    HashMap<String, Integer> getCartInfo();

    void setCartInfo(HashMap<String, Integer> cartInfo);

    boolean isNextPageLoading();

    void setNextPageLoading(boolean isNextPageLoading);

    void setTabNameWithEmptyProductView(String tabName);
}
