package com.bigbasket.mobileapp.activity.account.uiv3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.fragment.account.spendTrends.CategorySpentFragment;
import com.bigbasket.mobileapp.fragment.account.spendTrends.SavedFragment;
import com.bigbasket.mobileapp.fragment.account.spendTrends.SpentFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.OnObservableScrollEvent;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendSummary;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrends;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsCategoryExpRangeData;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsCategoryExpRangeInfo;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsDateRange;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsRangeData;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsSpentSavedRangeInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpendTrendsActivity extends BaseActivity implements OnObservableScrollEvent {

    private BBDrawerLayout mDrawerLayout;
    private SpendTrends mSpendTrends;
    private ArrayList<String> mCategoryDropdown;
    private Spinner mSpinnerMonthRange;
    private Spinner mSpinnerCategories;
    private View mLayoutSpendTrendsFilter, mLayoutSpendTrendsFilterEmpty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_spend_trends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.spendTrends));
        mDrawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        mLayoutSpendTrendsFilter = findViewById(R.id.layoutSpendTrendsFilter);
        mLayoutSpendTrendsFilterEmpty = findViewById(R.id.layoutSpendTrendsFilterEmpty);
        loadSpendTrends(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spend_trends_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter_spend_trends) {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {
        getSupportActionBar().setTitle(getString(R.string.spendTrends));
    }

    private void loadSpendTrends(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSpendTrends = savedInstanceState.getParcelable(Constants.SPENT_SAVED);
            if (mSpendTrends != null) {
                renderSpendTrends();
                return;
            }
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.spendTrends(new Callback<ApiResponse<SpendTrends>>() {
            @Override
            public void success(ApiResponse<SpendTrends> spendTrendsApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (spendTrendsApiResponse.status) {
                    case 0:
                        mSpendTrends = spendTrendsApiResponse.apiResponseContent;
                        renderSpendTrends();
                        break;
                    default:
                        handler.sendEmptyMessage(spendTrendsApiResponse.status, spendTrendsApiResponse.message, true);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private boolean isSpendTrendsEmpty() {
        return mSpendTrends == null || mSpendTrends.getSpentSavedRangeInfos() == null ||
                mSpendTrends.getSpentSavedRangeInfos().size() == 0;
    }

    private void toggleFilterLayoutVisibility() {
        if (isSpendTrendsEmpty()) {
            mLayoutSpendTrendsFilterEmpty.setVisibility(View.VISIBLE);
            mLayoutSpendTrendsFilter.setVisibility(View.GONE);
        } else {
            mLayoutSpendTrendsFilterEmpty.setVisibility(View.GONE);
            mLayoutSpendTrendsFilter.setVisibility(View.VISIBLE);
        }
    }

    private void renderSpendTrends() {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        if (isSpendTrendsEmpty()) {
            View spendTrendsEmptyView = getLayoutInflater().inflate(R.layout.uiv3_empty_spend_trends, contentFrame, false);
            contentFrame.addView(spendTrendsEmptyView);
            toggleFilterLayoutVisibility();
            return;
        }

        mSpinnerCategories = (Spinner) findViewById(R.id.spinnerCategories);

        // Initialize date range spinner
        mSpinnerMonthRange = (Spinner) findViewById(R.id.spinnerMonthRange);
        BBArrayAdapter<SpendTrendsDateRange> spendTrendsDateRangeArrayAdapter =
                new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSpendTrends.getDateRanges(), faceRobotoRegular,
                        getResources().getColor(R.color.uiv3_primary_text_color),
                        getResources().getColor(R.color.uiv3_primary_text_color));
        spendTrendsDateRangeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMonthRange.setAdapter(spendTrendsDateRangeArrayAdapter);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize category range spinner
        mCategoryDropdown = new ArrayList<>();
        mCategoryDropdown.add(Constants.ALL_CATEGORIES);
        for (Map.Entry<String, String> entry : mSpendTrends.getTopCategoryNameSlugMap().entrySet()) {
            mCategoryDropdown.add(entry.getKey());
        }
        BBArrayAdapter<String> spendTrendsCategoryAdapter =
                new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCategoryDropdown, faceRobotoRegular,
                        getResources().getColor(R.color.uiv3_primary_text_color),
                        getResources().getColor(R.color.uiv3_primary_text_color));
        spendTrendsCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCategories.setAdapter(spendTrendsCategoryAdapter);
        int defaultMonthIndx = SpendTrendsDateRange.getSelectedIndex(mSpendTrends.getDateRanges(), mSpendTrends.getDefaultRange());
        mSpinnerMonthRange.setSelection(defaultMonthIndx);
        renderCharts(mSpinnerMonthRange.getSelectedItemPosition(),
                mSpinnerCategories.getSelectedItemPosition());
    }

    private void renderCharts(int selectedMonthPosition, int selectedCategoryPosition) {

        String categoryName = mCategoryDropdown.get(selectedCategoryPosition);
        String categorySlug = categoryName.equals(Constants.ALL_CATEGORIES) ?
                "" : mSpendTrends.getTopCategoryNameSlugMap().get(categoryName);
        SpendTrendsDateRange selectedSpendTrendsDateRange = mSpendTrends.getDateRanges().get(selectedMonthPosition);

        SpendTrendsSpentSavedRangeInfo selectedSpentSavedRangeInfo = SpendTrendsSpentSavedRangeInfo.
                getSelectedSpentSavedRangeInfo(mSpendTrends.getSpentSavedRangeInfos(), selectedSpendTrendsDateRange);

        if (selectedSpentSavedRangeInfo == null) return;
        ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData = SpendTrendsSpentSavedRangeInfo.
                getFilteredSpendTrendsRangeData(selectedSpentSavedRangeInfo, categoryName);

        SpendTrendsCategoryExpRangeInfo selectedCategoryExpRangeInfo =
                SpendTrendsCategoryExpRangeInfo.getSelectedCategoryExpRangeInfo(mSpendTrends.getCategoryExpRangeInfos(), selectedSpendTrendsDateRange);

        if (selectedCategoryExpRangeInfo == null) return;
        ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData =
                SpendTrendsCategoryExpRangeInfo.getFilteredCategoryExpRangeData(selectedCategoryExpRangeInfo, categoryName);

        SpendTrendSummary selectedSummary = mSpendTrends.getSummary().
                get(selectedSpendTrendsDateRange.getRangeName()).get(categoryName);
        displayChartFragments(filteredSpentSavedRangeData, filteredCategoryExpRangeData, categoryName,
                selectedSummary, selectedSpendTrendsDateRange.getRangeVal());
    }

    private void displayChartFragments(ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData,
                                       ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData,
                                       String categoryName, SpendTrendSummary selectedSummary,
                                       int rangeVal) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        View base = getLayoutInflater().inflate(R.layout.uiv3_spend_trends_tab, contentFrame, false);

        final ArrayList<BBTab> bbTabs = new ArrayList<>();
        Bundle spentSavedBundle = new Bundle();
        spentSavedBundle.putParcelableArrayList(Constants.RANGE_DATA, filteredSpentSavedRangeData);
        spentSavedBundle.putString(Constants.TOP_CATEGORY, categoryName);
        spentSavedBundle.putParcelable(Constants.SUMMARY, selectedSummary);
        spentSavedBundle.putInt(Constants.RANGE_VAL, rangeVal);

        Bundle categorySpentBundle = new Bundle();
        categorySpentBundle.putParcelableArrayList(Constants.CATEGORY_SPENT, filteredCategoryExpRangeData);
        categorySpentBundle.putString(Constants.TOP_CATEGORY, categoryName);
        categorySpentBundle.putParcelable(Constants.SUMMARY, selectedSummary);

        bbTabs.add(new BBTab<>(getString(R.string.spent), SpentFragment.class, spentSavedBundle));
        bbTabs.add(new BBTab<>(getString(R.string.saved), SavedFragment.class, spentSavedBundle));
        bbTabs.add(new BBTab<>(getString(R.string.saved), CategorySpentFragment.class, categorySpentBundle));

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) base.findViewById(R.id.pagerCircles);
        circlePageIndicator.setViewPager(viewPager);
        circlePageIndicator.setFillColor(getResources().getColor(R.color.uiv3_action_bar_background));
        circlePageIndicator.setStrokeColor(getResources().getColor(R.color.uiv3_action_bar_background));
        circlePageIndicator.setPageColor(Color.WHITE);
        circlePageIndicator.setRadius(6 * getResources().getDisplayMetrics().density);

        contentFrame.removeAllViews();
        contentFrame.addView(base);
    }

    public void onApplyFilterButtonClicked(View v) {
        if (mSpendTrends == null || mCategoryDropdown == null) return;
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
        renderCharts(mSpinnerMonthRange.getSelectedItemPosition(),
                mSpinnerCategories.getSelectedItemPosition());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSpendTrends != null) {
            outState.putParcelable(Constants.SPENT_SAVED, mSpendTrends);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onScrollUp() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar.isShowing()) {
            actionBar.hide();
        }
    }

    @Override
    public void onScrollDown() {
        ActionBar actionBar = getSupportActionBar();
        if (!actionBar.isShowing()) {
            actionBar.show();
        }
    }
}
