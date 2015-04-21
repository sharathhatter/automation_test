package com.bigbasket.mobileapp.activity.product;

import android.os.Bundle;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.fragment.product.SearchFragment;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;


public class ProductListActivity extends BBActivity {

    @Override
    public void doSearch(String searchQuery) {
        searchQuery = searchQuery.trim();
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(searchQuery);
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SEARCH_QUERY, searchQuery);
        searchFragment.setArguments(bundle);
        addToMainLayout(searchFragment);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void syncBasket() {
        // Don't remove the IS_BASKET_DIRTY flag, as Fragment also needs to refresh, only update count
        new GetCartCountTask<>(this).startTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportActionBar().setSubtitle(null);
    }
}