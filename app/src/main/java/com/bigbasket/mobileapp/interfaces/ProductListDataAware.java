package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;

import java.util.ArrayList;
import java.util.List;

public interface ProductListDataAware {
    public ProductListData getProductListData();

    public void setProductListData(ProductListData productListData);

    public void updateData();

    public void updateProductList(List<Product> nextPageProducts);

    public boolean isNextPageLoading();

    public void setNextPageLoading(boolean isNextPageLoading);

    @NonNull
    public ArrayList<NameValuePair> getProductQueryParams();
}
