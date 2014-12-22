package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

public class SearchFragment extends ProductListAwareFragment {

    @Override
    public String getProductListSlug() {
        String searchQuery = getArguments().getString(Constants.SEARCH_QUERY);
        setTitle(searchQuery);
        return searchQuery;
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

    @Override
    public void updateData() {
        super.updateData();
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.QUERY, getProductListSlug());
        trackEvent(TrackingAware.SEARCH, map);
    }

    @Override
    public String getSourceName(){
        return TrackEventkeys.PRODUCT_SEARCH;
    }
}
