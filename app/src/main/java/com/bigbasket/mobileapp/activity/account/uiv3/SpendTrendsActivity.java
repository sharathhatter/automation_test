package com.bigbasket.mobileapp.activity.account.uiv3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.fragment.account.spendTrends.SpentFragment;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrends;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsCategoryExpRangeData;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsCategoryExpRangeInfo;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsDateRange;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsRangeData;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsSpentSavedRangeInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpendTrendsActivity extends BackButtonActivity {

    private boolean mIsFirstTime = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSpendTrends();
    }

    private void loadSpendTrends() {
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
                        renderSpendTrends(spendTrendsApiResponse.apiResponseContent);
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
                handler.handleRetrofitError(error);
            }
        });
    }

    private void renderSpendTrends(final SpendTrends spendTrends) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        if (spendTrends == null || spendTrends.getSpentSavedRangeInfos() == null ||
                spendTrends.getSpentSavedRangeInfos().size() == 0) {
            View spendTrendsEmptyView = getLayoutInflater().inflate(R.layout.uiv3_empty_spend_trends, null);
            contentFrame.addView(spendTrendsEmptyView);
            return;
        }

        final View base = getLayoutInflater().inflate(R.layout.uiv3_spend_trends, null);
        final Spinner spinnerCategories = (Spinner) base.findViewById(R.id.spinnerCategories);
        int defaultMonthIndx = SpendTrendsDateRange.getSelectedIndex(spendTrends.getDateRanges(), spendTrends.getDefaultRange());

        // Initialize date range spinner
        final Spinner spinnerMonthRange = new Spinner(this);
        BBArrayAdapter<SpendTrendsDateRange> spendTrendsDateRangeArrayAdapter =
                new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, spendTrends.getDateRanges(), faceRobotoRegular,
                        Color.WHITE, getResources().getColor(R.color.uiv3_primary_text_color));
        spendTrendsDateRangeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthRange.setAdapter(spendTrendsDateRangeArrayAdapter);
        Toolbar toolbar = getToolbar();
        toolbar.addView(spinnerMonthRange);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize category range spinner
        final ArrayList<String> categoryDropDown = new ArrayList<>();
        categoryDropDown.add(Constants.ALL_CATEGORIES);
        for (Map.Entry<String, String> entry : spendTrends.getTopCategoryNameSlugMap().entrySet()) {
            categoryDropDown.add(entry.getKey());
        }
        BBArrayAdapter<String> spendTrendsCategoryAdapter =
                new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryDropDown, faceRobotoRegular,
                        Color.WHITE, getResources().getColor(R.color.uiv3_primary_text_color));
        spendTrendsCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(spendTrendsCategoryAdapter);

        contentFrame.addView(base);

        // Set on-selection events
        spinnerMonthRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderCharts(base, spendTrends, categoryDropDown, position, spinnerCategories.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIsFirstTime) {
                    mIsFirstTime = false;
                    return;
                }
                renderCharts(base, spendTrends, categoryDropDown,
                        spinnerMonthRange.getSelectedItemPosition(), position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void renderCharts(View base, SpendTrends spendTrends, ArrayList<String> categoryDropDown,
                              int selectedMonthPosition, int selectedCategoryPosition) {

        LinearLayout layoutSpendTrendsTab = (LinearLayout) base.findViewById(R.id.layoutSpendTrendsTab);
        layoutSpendTrendsTab.removeAllViews();

        String categoryName = categoryDropDown.get(selectedCategoryPosition);
        String categorySlug = categoryName.equals(Constants.ALL_CATEGORIES) ?
                "" : spendTrends.getTopCategoryNameSlugMap().get(categoryName);
        SpendTrendsDateRange selectedSpendTrendsDateRange = spendTrends.getDateRanges().get(selectedMonthPosition);

        SpendTrendsSpentSavedRangeInfo selectedSpentSavedRangeInfo = SpendTrendsSpentSavedRangeInfo.
                getSelectedSpentSavedRangeInfo(spendTrends.getSpentSavedRangeInfos(), selectedSpendTrendsDateRange);

        if (selectedSpentSavedRangeInfo == null) return;
        ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData = SpendTrendsSpentSavedRangeInfo.
                getFilteredSpendTrendsRangeData(selectedSpentSavedRangeInfo, categoryName);

        SpendTrendsCategoryExpRangeInfo selectedCategoryExpRangeInfo =
                SpendTrendsCategoryExpRangeInfo.getSelectedCategoryExpRangeInfo(spendTrends.getCategoryExpRangeInfos(), selectedSpendTrendsDateRange);

        if (selectedCategoryExpRangeInfo == null) return;
        ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData =
                SpendTrendsCategoryExpRangeInfo.getFilteredCategoryExpRangeData(selectedCategoryExpRangeInfo, categoryName);

        displayChartFragments(layoutSpendTrendsTab, filteredSpentSavedRangeData, filteredCategoryExpRangeData, categoryName);
    }

    private void displayChartFragments(LinearLayout layoutSpendTrendsTab, ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData,
                                       ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData,
                                       String categoryName) {
        View base = getLayoutInflater().inflate(R.layout.uiv3_spend_trends_tab, null);

        final ArrayList<BBTab> bbTabs = new ArrayList<>();
        Bundle spentBundle = new Bundle();
        spentBundle.putParcelableArrayList(Constants.RANGE_DATA, filteredSpentSavedRangeData);
        spentBundle.putString(Constants.TOP_CATEGORY, categoryName);
        bbTabs.add(new BBTab<>(getString(R.string.spent), SpentFragment.class, spentBundle));
        //bbTabs.add(new BBTab<>(getString(R.string.saved), SpendTrendsPieChartFragment.class, null));

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setViewPager(viewPager);
        layoutSpendTrendsTab.addView(base);
    }
}
