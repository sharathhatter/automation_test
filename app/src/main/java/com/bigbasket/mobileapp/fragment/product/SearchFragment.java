package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;

public class SearchFragment extends ProductListAwareFragment {

    @Override
    public String getProductListSlug() {
        return getArguments().getString(Constants.SEARCH_QUERY);
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.SEARCH.get();
    }

    @Override
    public String getTitle() {
        return "Search Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SearchFragment.class.getName();
    }
}
