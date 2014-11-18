package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;


public class ShopInShopFragment extends ProductListAwareFragment {

    public String getProductQueryType() {
        return ProductListType.SHOP.get();
    }

    public String getProductListSlug() {
        Bundle bundle = getArguments();
        String shopId = bundle != null ? bundle.getString(Constants.SHOP_ID) : null;
        shopId = TextUtils.isEmpty(shopId) ? "express" : shopId;
        return shopId;
    }

    @Override
    public String getTitle() {
        return "Shop Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopInShopFragment.class.getName();
    }
}