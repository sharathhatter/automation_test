package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class SearchFragment extends ProductListAwareFragment {

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        String searchQuery = getArguments().getString(Constants.SEARCH_QUERY);
        setTitle(searchQuery);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery));
        return nameValuePairs;
    }

    @Override
    @Nullable
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
    }

    @Override
    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_PRODUCT_SEARCH;
    }
}
