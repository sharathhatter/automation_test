package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;


public class CategoryProductsFragment extends ProductListAwareFragment {

    @Override
    public String getProductListSlug() {
        Bundle bundle = getArguments();
        return bundle.getString("slug_name_category");
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.CATEGORY.get();
    }

    @Override
    public String getTitle() {
        return "Browse by Category";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return CategoryProductsFragment.class.getName();
    }

    @Override
    public String getSourceName() {
        return TrackEventkeys.CATEGORY_LANDING;
    }
}