package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendSummary;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsRangeData;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

public class SpentFragment extends BaseSpendTrendsFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData = getArguments().
                getParcelableArrayList(Constants.RANGE_DATA);
        String categoryName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        int rangeVal = getArguments().getInt(Constants.RANGE_VAL);
        SpendTrendSummary summary = getArguments().getParcelable(Constants.SUMMARY);
        displayBarChart(filteredSpentSavedRangeData, categoryName, rangeVal, summary);
    }

    private void displayBarChart(ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData, String categoryName,
                                 int rangeVal, SpendTrendSummary summary) {
        int[] yAxisValues = new int[filteredSpentSavedRangeData.size()];
        String[] xAxisLabels = new String[filteredSpentSavedRangeData.size()];
        for (int i = 0; i < filteredSpentSavedRangeData.size(); i++) {
            SpendTrendsRangeData spendTrendsRangeData = filteredSpentSavedRangeData.get(i);
            yAxisValues[i] = spendTrendsRangeData.getSpent();
            xAxisLabels[i] = spendTrendsRangeData.getMonth();
        }
        displayBarChart(xAxisLabels, yAxisValues, getString(R.string.howMuchSpent),
                getString(R.string.spent), categoryName, getString(R.string.amountInRs));
        setSummaryTextView(rangeVal, summary, true);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SpentFragment.class.getName();
    }
}
