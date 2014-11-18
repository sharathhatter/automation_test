package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;


public class BrowseByOffersFragment extends ProductListAwareFragment {

    @Override
    public String getProductListSlug() {
        return Constants.ALL_OFFERS;
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.CATEGORY.get();
    }

    @Override
    public String getTitle() {
        return "Browse by Offers";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return BrowseByOffersFragment.class.getName();
    }
}