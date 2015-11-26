package com.bigbasket.mobileapp.handler.click.basket;

import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;

public class OnProductBasketActionListener extends OnBasketActionAbstractListener {
    public OnProductBasketActionListener(@BasketOperation.Mode int basketOperation) {
        super(basketOperation);
    }

    @Override
    protected Product getProduct(View v) {
        return (Product) v.getTag(R.id.basket_op_product_tag_id);
    }
}
