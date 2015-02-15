package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class CategoryProductsFragment extends ProductListAwareFragment {

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        Bundle bundle = getArguments();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG,
                bundle.getString(Constants.SLUG_NAME_CATEGORY)));
        return nameValuePairs;
    }

    @Override
    protected ArrayList<FilteredOn> getProductRefinedByFilter() {
        Bundle bundle = getArguments();
        if (!TextUtils.isEmpty(bundle.getString(Constants.FILTER))) {
            FilteredOn filteredOn = new FilteredOn((bundle.getString(Constants.FILTER)));
            ArrayList<FilteredOn> filteredOnArrayList = new ArrayList<>();
            filteredOnArrayList.add(filteredOn);
            return filteredOnArrayList;
        }
        return null;
    }

    @Override
    protected String getProductRefinedBySortedOn() {
        Bundle bundle = getArguments();
        if (!TextUtils.isEmpty(bundle.getString(Constants.SORT_BY)))
            return bundle.getString(Constants.SORT_BY);
        return null;
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