package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.ProductTabData;

public interface ProductListDataAware {
    void setProductTabData(ProductTabData productTabData);

    boolean isNextPageLoading();

    void setNextPageLoading(boolean isNextPageLoading);
}
