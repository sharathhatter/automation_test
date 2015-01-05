package com.bigbasket.mobileapp.fragment.account;

import android.graphics.Color;
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
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

public class SpendTrendsPieChartFragment extends AbstractFragment {

    /**
     * Colors to be used for the pie slices.
     */
    private static final int[] COLORS = new int[]{Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN};
    /**
     * The main series that will include all the data.
     */
    private CategorySeries mSeries = new CategorySeries("");
    /**
     * The main renderer for the main dataset.
     */
    private DefaultRenderer mRenderer = new DefaultRenderer();
    private GraphicalView mChartView;

    private void initChart() {
        // set the start angle for the first slice in the pie chart
        mRenderer.setStartAngle(180);
// display values on the pie slices
        mRenderer.setDisplayValues(true);
    }

    private void addSampleData() {
        for (int i = 0; i < COLORS.length; i++) {
            mSeries.add("Series " + (i + 1), 10);
            SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
            simpleSeriesRenderer.setColor(COLORS[i]);
            mRenderer.addSeriesRenderer(simpleSeriesRenderer);
            mChartView.repaint();
        }
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
        mChartView = ChartFactory.getPieChartView(getActivity(), mSeries, mRenderer);
        LinearLayout layoutBarChart = (LinearLayout) base.findViewById(R.id.layoutBarChart);
        layoutBarChart.addView(mChartView);
        initChart();
        addSampleData();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SpendTrendsPieChartFragment.class.getName();
    }
}
