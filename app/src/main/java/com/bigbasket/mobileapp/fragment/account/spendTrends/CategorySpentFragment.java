package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendSummary;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsCategoryExpRangeData;
import com.bigbasket.mobileapp.util.Constants;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.ArrayList;

public class CategorySpentFragment extends BaseSpendTrendsFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_spend_trends_chart, container, false);
    }

    @Override
    public ObservableScrollView getObservableScrollView() {
        assert getView() != null;
        return (ObservableScrollView) getView().findViewById(R.id.scrollViewSpendTrends);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData =
                getArguments().getParcelableArrayList(Constants.CATEGORY_SPENT);
        String categoryName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        SpendTrendSummary summary = getArguments().getParcelable(Constants.SUMMARY);
        displayPieChart(filteredCategoryExpRangeData, categoryName, summary);
    }

    private void displayPieChart(ArrayList<SpendTrendsCategoryExpRangeData> filteredCategoryExpRangeData,
                                 String categoryName, SpendTrendSummary summary) {
        if (getActivity() == null || getView() == null) return;
        View base = getView();
        LinearLayout layoutBarChart = (LinearLayout) base.findViewById(R.id.layoutChart);
        layoutBarChart.removeAllViews();

        CategorySeries categorySeries = new CategorySeries(categoryName);

        // Add data to pie chart with its name and value
        for (SpendTrendsCategoryExpRangeData spendTrendsCategoryExpRangeData : filteredCategoryExpRangeData) {
            categorySeries.add(spendTrendsCategoryExpRangeData.getCategory(),
                    spendTrendsCategoryExpRangeData.getSpent());
        }

        // Create a renderer for the pie chart
        int[] colors = getPieChartColors(filteredCategoryExpRangeData.size(),
                getResources().getColor(R.color.pie_chart_base_color), false);
        DefaultRenderer renderer = new DefaultRenderer();

        for (int i = 0; i < filteredCategoryExpRangeData.size(); i++) {
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(colors[i]);
            seriesRenderer.setDisplayChartValues(true);
            renderer.addSeriesRenderer(seriesRenderer);
        }

        renderer.setTextTypeface(faceRobotoRegular);
        renderer.setChartTitleTextSize(getResources().getDimension(R.dimen.primary_text_size));
        renderer.setLabelsColor(getResources().getColor(R.color.uiv3_primary_text_color));
        renderer.setZoomButtonsVisible(true);

        TextView txtChartTitle = (TextView) base.findViewById(R.id.txtChartTile);
        txtChartTitle.setTypeface(faceRobotoRegular);
        txtChartTitle.setText(getString(R.string.whatCategoryMoneySpent));

        GraphicalView graphicalView = ChartFactory.getPieChartView(getActivity(), categorySeries, renderer);
        layoutBarChart.addView(graphicalView);
        setSummaryTextView(-1, summary, false);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return CategorySpentFragment.class.getName();
    }
}
