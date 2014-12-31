package com.bigbasket.mobileapp.fragment.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class SpendTrendsBarChartFragment extends AbstractFragment {
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addSampleData() {
        mCurrentSeries.add(1, 2);
        mCurrentSeries.add(2, 3);
        mCurrentSeries.add(3, 2);
        mCurrentSeries.add(4, 5);
        mCurrentSeries.add(5, 4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_spend_trends_bar_chart, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeScroll();
        initializeBarChart();
    }

    private void initializeScroll() {
        if (getActivity() == null || getView() == null) return;

        final ObservableScrollView scrollViewSpendTrends = (ObservableScrollView) getView().findViewById(R.id.scrollViewSpendTrends);

        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        scrollViewSpendTrends.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int i, boolean b, boolean b2) {

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
                if (scrollState == ScrollState.UP) {
                    if (actionBar.isShowing()) {
                        actionBar.hide();
                    }
                } else if (scrollState == ScrollState.DOWN) {
                    if (!actionBar.isShowing()) {
                        actionBar.show();
                    }
                }
            }
        });
    }

    private void initializeBarChart() {
        if (getActivity() == null || getView() == null) return;
        View base = getView();
        initChart();
        addSampleData();
        LinearLayout layoutBarChart = (LinearLayout) base.findViewById(R.id.layoutBarChart);
        GraphicalView graphicalView = ChartFactory.getBarChartView(getActivity(), mDataset, mRenderer, BarChart.Type.DEFAULT);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.bar_chart_height));
        graphicalView.setLayoutParams(chartParams);
        layoutBarChart.addView(graphicalView);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SpendTrendsBarChartFragment.class.getName();
    }
}
