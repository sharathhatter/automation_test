package com.bigbasket.mobileapp.adapter.uiv2.product;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;

import java.util.List;

public class ProductListAdapter extends com.bigbasket.mobileapp.adapter.product.ProductListAdapter {
    public ProductListAdapter(List<Product> products, String baseImgUrl, BaseActivity context,
                              ProductViewDisplayDataHolder productViewDisplayDataHolder) {
        super(products, baseImgUrl, context, productViewDisplayDataHolder, null, 1);
    }
}
