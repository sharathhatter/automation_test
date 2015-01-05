package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendsRangeData;
import com.bigbasket.mobileapp.util.Constants;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class SavedFragment extends BaseSpendTrendsFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_spend_trends_bar_chart, container, false);
    }

    @Override
    public ObservableScrollView getObservableScrollView() {
        assert getView() != null;
        return (ObservableScrollView) getView().findViewById(R.id.scrollViewSpendTrends);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData = getArguments().
                getParcelableArrayList(Constants.RANGE_DATA);
        String categoryName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        initializeScroll();
        displayBarChart(filteredSpentSavedRangeData, categoryName);
    }

    private void displayBarChart(ArrayList<SpendTrendsRangeData> filteredSpentSavedRangeData,
                                 String categoryName) {
        if (getActivity() == null || getView() == null) return;
        View base = getView();
        LinearLayout layoutBarChart = (LinearLayout) base.findViewById(R.id.layoutBarChart);

        // Creating XYSeries for amount spent
        XYSeries series = new XYSeries(getString(R.string.spent));
        // Add data to XYSeries
        for (int i = 0; i < filteredSpentSavedRangeData.size(); i++) {
            series.add(i, filteredSpentSavedRangeData.get(i).getSaved());
        }
        // Creating data-set to hold the series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        // Creating XYRenderer to customize spend series
        XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
        seriesRenderer.setColor(getResources().getColor(R.color.bar_chart_color));
        seriesRenderer.setFillPoints(true);
        seriesRenderer.setLineWidth(2);
        seriesRenderer.setDisplayChartValues(true);

        // Creating XYMultipleSeriesRenderer to customize the whole graph
        XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        multipleSeriesRenderer.setXLabels(0);
        multipleSeriesRenderer.setChartTitle(getString(R.string.howMuchSaved));
        multipleSeriesRenderer.setXTitle(categoryName);
        multipleSeriesRenderer.setYTitle("Saved in Rs");
        multipleSeriesRenderer.setBackgroundColor(getResources().getColor(R.color.white));
        multipleSeriesRenderer.setLabelsColor(getResources().getColor(R.color.uiv3_primary_text_color));
        multipleSeriesRenderer.setTextTypeface(faceRobotoRegular);
        for (int i = 0; i < filteredSpentSavedRangeData.size(); i++) {
            multipleSeriesRenderer.addXTextLabel(i, filteredSpentSavedRangeData.get(i).getMonth());
        }
        multipleSeriesRenderer.addSeriesRenderer(seriesRenderer);

        GraphicalView graphicalView = ChartFactory.getBarChartView(getActivity(), dataset,
                multipleSeriesRenderer, BarChart.Type.DEFAULT);
        layoutBarChart.addView(graphicalView);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SpentFragment.class.getName();
    }
}
