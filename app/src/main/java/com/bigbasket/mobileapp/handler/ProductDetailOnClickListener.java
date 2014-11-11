package com.bigbasket.mobileapp.handler;

import android.os.Bundle;
import android.view.View;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.util.Constants;

public class ProductDetailOnClickListener implements View.OnClickListener {
    private String skuId;
    private BaseFragment fragment;

    public ProductDetailOnClickListener(int skuId, BaseFragment context) {
        this(String.valueOf(skuId), context);
    }

    public ProductDetailOnClickListener(String skuId, BaseFragment context) {
        this.skuId = skuId;
        this.fragment = context;
    }

    @Override
    public void onClick(View v) {
        if (fragment == null) return;
        ProductDetailFragment productDetailFragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(Constants.SKU_ID, skuId);
        productDetailFragment.setArguments(args);
        fragment.changeFragment(productDetailFragment);
    }
}
