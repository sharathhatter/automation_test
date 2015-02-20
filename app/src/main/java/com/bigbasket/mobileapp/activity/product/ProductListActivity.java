package com.bigbasket.mobileapp.activity.product;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.product.FilterByAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.FilterDisplayAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.task.GetCartCountTask;
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

        if (isFilteredOnEmpty()) {
            btnClearFilter.setVisibility(View.GONE);
        } else {
            btnClearFilter.setVisibility(View.VISIBLE);
        }

        if (mFilterOptionItems == null || mFilterOptionItems.size() == 0) {
            LayoutInflater inflater = getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.uiv3_empty_data_text, listFilter, false);
            TextView txtEmptyDataMsg = (TextView) emptyView.findViewById(R.id.txtEmptyDataMsg);
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
        if (productListAwareFragment != null) {
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
    public String getScreenTag() {
        return TrackEventkeys.PRESCRIPTION_LISTING_SCREEN;
    }

    public void syncBasket() {
        // Don't remove the IS_BASKET_DIRTY flag, as Fragment also needs to refresh, only update count
        new GetCartCountTask<>(this, true).startTask();
    }
}