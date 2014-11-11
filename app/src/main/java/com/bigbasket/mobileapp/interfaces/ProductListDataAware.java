package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;

import java.util.List;

public interface ProductListDataAware {
    ProductListData getProductListData();

    void setProductListData(ProductListData productListData);

    void updateData();

    void updateProductList(List<Product> nextPageProducts);
}
