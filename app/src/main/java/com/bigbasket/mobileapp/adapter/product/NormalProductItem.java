package com.bigbasket.mobileapp.adapter.product;

import com.bigbasket.mobileapp.model.product.Product;

/**
 * Created by muniraju on 16/12/15.
 */
public class NormalProductItem extends AbstractProductItem {

    private final Product product;

    public NormalProductItem(Product product) {
        super(ProductListRecyclerAdapter.VIEW_TYPE_DATA);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
