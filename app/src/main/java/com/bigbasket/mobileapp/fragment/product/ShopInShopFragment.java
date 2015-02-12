package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;


public class ShopInShopFragment extends ProductListAwareFragment {

    public String getProductQueryType() {
        return ProductListType.SHOP.get();
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        Bundle bundle = getArguments();
        String shopSlug = bundle != null ? bundle.getString(Constants.SLUG) : null;
        shopSlug = TextUtils.isEmpty(shopSlug) ? "express" : shopSlug;
        return shopSlug;
    }

    @Override
    public String getTitle() {
        if(!TextUtils.isEmpty(getArguments().getString(Constants.TRACK_EVENT_NAME)))
            trackEvent("Browse." + getArguments().getString(Constants.TRACK_EVENT_NAME), null);
        return "Shop Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopInShopFragment.class.getName();
    }

    @Override
    public String getSourceName() {
        return TrackEventkeys.SHOP_IN_SHOP;
    }
}