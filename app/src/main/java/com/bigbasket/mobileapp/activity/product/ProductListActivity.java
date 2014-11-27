package com.bigbasket.mobileapp.activity.product;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.product.FilterByAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.FilterDisplayAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class ProductListActivity extends BBActivity implements FilterDisplayAware {

    private String mFragmentTag;
    private Map<String, Set<String>> mFilteredOn;
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
    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            menuInflater.inflate(R.menu.search_menu, menu);

            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();

            // Setting the search listener
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                private boolean extended = false;

                @Override
                public void onClick(View v) {
                    if (!extended) {
                        extended = true;
                        ViewGroup.LayoutParams lp = v.getLayoutParams();
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                }
            });
        } else {
            menuInflater.inflate(R.menu.search_menu_pre_honeycomb, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_apply_filter && getDrawerLayout() != null) {
            BBDrawerLayout drawerLayout = getDrawerLayout();
            drawerLayout.closeDrawer(Gravity.LEFT);
            drawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        } else if (item.getItemId() == R.id.action_sort_products) {
            closeFilterDrawer();
            ProductListAwareFragment productListAwareFragment = getProductListAwareFragment();
            if (productListAwareFragment != null) {
                productListAwareFragment.onSortViewRequested();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setFilterView(ArrayList<FilterOptionCategory> filterOptionCategories,
                              Map<String, Set<String>> filteredOn, String fragmentTag) {
        mFilterOptionItems = filterOptionCategories;
        mFilteredOn = filteredOn;
        mFragmentTag = fragmentTag;
        Button btnApplyFilter = (Button) findViewById(R.id.btnApplyFilter);
        Button btnClearFilter = (Button) findViewById(R.id.btnClearFilters);
        ExpandableListView listFilter = (ExpandableListView) findViewById(R.id.listFilter);

        if (isFilteredOnEmpty()) {
            btnClearFilter.setVisibility(View.GONE);
        } else {
            btnClearFilter.setVisibility(View.VISIBLE);
        }

        if (mFilterOptionItems == null || mFilterOptionItems.size() == 0) {
            LayoutInflater inflater = getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.uiv3_empty_data_text, null);
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
        for (Map.Entry<String, Set<String>> filteredOnSet : mFilteredOn.entrySet()) {
            if (filteredOnSet.getValue() != null && filteredOnSet.getValue().size() > 0) {
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
            for (Map.Entry<String, Set<String>> filteredOnOption : mFilteredOn.entrySet()) {
                filteredOnOption.getValue().clear();
            }
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
}