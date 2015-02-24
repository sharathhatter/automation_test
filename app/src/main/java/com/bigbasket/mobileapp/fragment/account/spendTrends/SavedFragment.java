package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendSummary;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsRangeData;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

public class SavedFragment extends BaseSpendTrendsFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData = getArguments().
                getParcelableArrayList(Constants.RANGE_DATA);
        String categoryName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        SpendTrendSummary summary = getArguments().getParcelable(Constants.SUMMARY);
        displayBarChart(filteredSpentSavedRangeData, categoryName, summary);
    }

    private void displayBarChart(ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData, String categoryName,
                                 SpendTrendSummary summary) {
        int[] yAxisValues = new int[filteredSpentSavedRangeData.size()];
        String[] xAxisLabels = new String[filteredSpentSavedRangeData.size()];
        for (int i = 0; i < filteredSpentSavedRangeData.size(); i++) {
            SpendTrendsRangeData spendTrendsRangeData = filteredSpentSavedRangeData.get(i);
            yAxisValues[i] = spendTrendsRangeData.getSaved();
            xAxisLabels[i] = spendTrendsRangeData.getMonth();
        }
        displayBarChart(xAxisLabels, yAxisValues, getString(R.string.howMuchSaved),
                getString(R.string.saved), categoryName, getString(R.string.amountInRs));
        setSummaryTextView(-1, summary, false);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SpentFragment.class.getName();
    }
}
