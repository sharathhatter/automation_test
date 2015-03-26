package com.bigbasket.mobileapp.activity.product;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.adapter.product.FilterByAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.fragment.product.SearchFragment;
import com.bigbasket.mobileapp.interfaces.FilterDisplayAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

import java.util.ArrayList;


public class ProductListActivity extends BBActivity implements FilterDisplayAware {

    private String mFragmentTag;
    private ArrayList<FilteredOn> mFilteredOn;
    private ArrayList<FilterOptionCategory> mFilterOptionItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_search_layout;
    }

    @Override
    public void showFilters() {
        BBDrawerLayout drawerLayout = getDrawerLayout();
        if (drawerLayout == null) return;
        drawerLayout.closeDrawer(Gravity.LEFT);
        ProductListAwareFragment productListAwareFragment = getProductListAwareFragment();
        if (productListAwareFragment != null) {
            if (productListAwareFragment.getProductListData() == null ||
                    productListAwareFragment.getProductListData().areFiltersEmpty()) {
                Toast.makeText(getCurrentActivity(), getString(R.string.noFilterOptions), Toast.LENGTH_SHORT).show();
            } else {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        }
    }

    @Override
    public void showSortOptions() {
        closeFilterDrawer();
        ProductListAwareFragment productListAwareFragment = getProductListAwareFragment();
        if (productListAwareFragment != null) {
            productListAwareFragment.onSortViewRequested();
        }
    }

    @Override
    public void setFilterView(ArrayList<FilterOptionCategory> filterOptionCategories,
                              ArrayList<FilteredOn> filteredOn, String fragmentTag) {
        mFilterOptionItems = filterOptionCategories;
        mFilteredOn = filteredOn;
        mFragmentTag = fragmentTag;
        TextView lblFilter = (TextView) findViewById(R.id.lblFilter);
        lblFilter.setTypeface(faceRobotoRegular);
        Button btnApplyFilter = (Button) findViewById(R.id.btnApplyFilter);
        btnApplyFilter.setTypeface(faceRobotoRegular);
        Button btnClearFilter = (Button) findViewById(R.id.btnClearFilters);
        btnClearFilter.setTypeface(faceRobotoRegular);
        ExpandableListView listFilter = (ExpandableListView) findViewById(R.id.listFilter);
        LinearLayout layoutFilterButtons = (LinearLayout) findViewById(R.id.layoutFilterButtons);


        if (isFilteredOnEmpty()) {
            btnClearFilter.setVisibility(View.GONE);
            btnApplyFilter.setBackgroundResource(R.drawable.ribbon_btn);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutFilterButtons.setLayoutParams(params);
        } else {
            btnClearFilter.setVisibility(View.VISIBLE);
            btnApplyFilter.setBackgroundResource(R.drawable.primary_btn_bkg);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int)getResources().getDimension(R.dimen.padding_mini),
                    (int)getResources().getDimension(R.dimen.padding_small),
                    (int)getResources().getDimension(R.dimen.padding_mini),
                    (int)getResources().getDimension(R.dimen.padding_small));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutFilterButtons.setLayoutParams(params);
        }

        if (mFilterOptionItems == null || mFilterOptionItems.size() == 0) {
            LayoutInflater inflater = getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.uiv3_empty_data_text, listFilter, false);
            TextView txtEmptyDataMsg = (TextView) emptyView.findViewById(R.id.txtEmptyMsg1);
            txtEmptyDataMsg.setText("Nothing to filter!");
            txtEmptyDataMsg.setTextColor(getResources().getColor(R.color.white));
            listFilter.setEmptyView(emptyView);
            btnApplyFilter.setVisibility(View.GONE);
        } else {
            btnApplyFilter.setVisibility(View.VISIBLE);
            FilterByAdapter filterByAdapter = new FilterByAdapter(mFilterOptionItems, mFilteredOn,
                    this);
            listFilter.setAdapter(filterByAdapter);
        }
    }

    private boolean isFilteredOnEmpty() {
        if (mFilteredOn == null || mFilteredOn.size() == 0) return true;
        for (FilteredOn filteredOn : mFilteredOn) {
            if (filteredOn.getFilterValues() != null && filteredOn.getFilterValues().size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void onApplyFilterButtonClicked(View view) {
        if (mFilterOptionItems == null || mFragmentTag == null) return;
        closeFilterDrawer();

        ProductListAwareFragment productListAwareFragment = getProductListAwareFragment();
        if (productListAwareFragment != null && mFilteredOn.size()>0) {
            productListAwareFragment.onFilterApplied(mFilteredOn);
        }
    }

    private ProductListAwareFragment getProductListAwareFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        int backStackEntryCount = supportFragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            Fragment supportFragment = getSupportFragmentManager().getFragments().
                    get(backStackEntryCount - 1);
            if (supportFragment instanceof ProductListAwareFragment) {
                return (ProductListAwareFragment) supportFragment;
            }
        }
        return null;
    }

    public void onClearFilterButtonClicked(View view) {
        closeFilterDrawer();
        if (mFilteredOn != null) {
            mFilteredOn = null;
        }

        if (mFilterOptionItems != null) {
            for (FilterOptionCategory filterOptionCategory : mFilterOptionItems) {
                for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                    filterOptionItem.setSelected(false);
                }
            }
        }
        ProductListAwareFragment productListAwareFragment = getProductListAwareFragment();
        if (productListAwareFragment != null) {
            productListAwareFragment.onFilterApplied(mFilteredOn);
        }
    }

    private void closeFilterDrawer() {
        if (getDrawerLayout() != null) {
            getDrawerLayout().closeDrawer(Gravity.RIGHT);
        }
    }

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
    public void onBackPressed(){
        if(getDrawerLayout().isDrawerOpen(Gravity.RIGHT)){
            getDrawerLayout().closeDrawer(Gravity.RIGHT);
        } else if(getDrawerLayout().isDrawerOpen(Gravity.LEFT)){
            getDrawerLayout().closeDrawer(Gravity.LEFT);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void syncBasket() {
        // Don't remove the IS_BASKET_DIRTY flag, as Fragment also needs to refresh, only update count
        new GetCartCountTask<>(this, true).startTask();
    }
}