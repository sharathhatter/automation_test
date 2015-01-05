package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrendSummary;
import com.bigbasket.mobileapp.util.UIUtil;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public abstract class BaseSpendTrendsFragment extends AbstractFragment {
    public abstract ObservableScrollView getObservableScrollView();

    public void initializeScroll() {
        if (getActivity() == null || getView() == null) return;

        final ObservableScrollView scrollViewSpendTrends = getObservableScrollView();

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

    public void displayBarChart(String[] xAxisLabels, int[] yAxisValue,
                                String chartTitle, String chartLegend,
                                String xTitle, String yTitle) {
        if (getActivity() == null || getView() == null) return;
        View base = getView();
        LinearLayout layoutBarChart = (LinearLayout) base.findViewById(R.id.layoutChart);
        layoutBarChart.removeAllViews();

        // Creating XYSeries for amount spent
        XYSeries series = new XYSeries(chartLegend);
        // Add data to XYSeries
        for (int i = 0; i < yAxisValue.length; i++) {
            series.add(i, yAxisValue[i]);
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
        multipleSeriesRenderer.setChartTitle(chartTitle);
        multipleSeriesRenderer.setXTitle(xTitle);
        multipleSeriesRenderer.setYTitle(yTitle);
        multipleSeriesRenderer.setApplyBackgroundColor(true);
        multipleSeriesRenderer.setBackgroundColor(Color.WHITE);
        multipleSeriesRenderer.setMarginsColor(Color.WHITE);
        multipleSeriesRenderer.setLabelsColor(Color.BLACK);
        multipleSeriesRenderer.setTextTypeface(faceRobotoRegular);
        multipleSeriesRenderer.setXLabelsColor(Color.BLACK);
        multipleSeriesRenderer.setYLabelsColor(0, Color.BLACK);
        multipleSeriesRenderer.setBarSpacing(2);
        multipleSeriesRenderer.setChartTitleTextSize(getResources().getDimension(R.dimen.primary_text_size));
        for (int i = 0; i < xAxisLabels.length; i++) {
            multipleSeriesRenderer.addXTextLabel(i, xAxisLabels[i]);
        }
        multipleSeriesRenderer.addSeriesRenderer(seriesRenderer);

        GraphicalView graphicalView = ChartFactory.getBarChartView(getActivity(), dataset,
                multipleSeriesRenderer, BarChart.Type.DEFAULT);
        layoutBarChart.addView(graphicalView);
    }

    public void setSummaryTextView(int rangeVal, SpendTrendSummary summary, boolean showAmountSpentPerMonth) {
        View base = getView();
        if (base == null) return;
        TextView lblAmountSpend = (TextView) base.findViewById(R.id.lblAmountSpend);
        TextView txtAmountSpend = (TextView) base.findViewById(R.id.txtAmountSpend);
        TextView lblAmountSaved = (TextView) base.findViewById(R.id.lblAmountSaved);
        TextView txtAmountSaved = (TextView) base.findViewById(R.id.txtAmountSaved);
        TextView lblAmountSpentPerMonth = (TextView) base.findViewById(R.id.lblAmountSpentPerMonth);
        TextView txtAmountSpentPerMonth = (TextView) base.findViewById(R.id.txtAmountSpentPerMonth);

        if (summary == null) {
            lblAmountSpend.setVisibility(View.GONE);
            txtAmountSpend.setVisibility(View.GONE);
            lblAmountSaved.setVisibility(View.GONE);
            txtAmountSaved.setVisibility(View.GONE);
            lblAmountSpentPerMonth.setVisibility(View.GONE);
            txtAmountSpentPerMonth.setVisibility(View.GONE);
            return;
        }

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) numberFormat).setDecimalFormatSymbols(decimalFormatSymbols);

        lblAmountSpend.setTypeface(faceRobotoRegular);
        txtAmountSpend.setTypeface(faceRobotoRegular);
        txtAmountSpend.setText(UIUtil.asRupeeSpannable(numberFormat.format(summary.getSpent()), faceRupee));

        lblAmountSaved.setTypeface(faceRobotoRegular);
        txtAmountSaved.setTypeface(faceRobotoRegular);
        txtAmountSaved.setText(UIUtil.asRupeeSpannable(numberFormat.format(summary.getSaved()), faceRupee));

        if (showAmountSpentPerMonth) {
            lblAmountSpentPerMonth.setTypeface(faceRobotoRegular);
            txtAmountSpentPerMonth.setTypeface(faceRobotoRegular);
            int amtSpentPerMonth = (int) Math.round((double) summary.getSpent() / (double) rangeVal);
            txtAmountSpentPerMonth.setText(UIUtil.asRupeeSpannable(numberFormat.format(amtSpentPerMonth), faceRupee));
        } else {
            lblAmountSpentPerMonth.setVisibility(View.GONE);
            txtAmountSpentPerMonth.setVisibility(View.GONE);
        }
    }

    public int[] getPieChartColors(int numPieSlices, int baseColor,
                                   boolean adjacentColors) {
        // Inspiration http://stackoverflow.com/a/19389478 and http://stackoverflow.com/a/19389478

        int[] colors = new int[numPieSlices];
        colors[0] = baseColor;
        float hsv[] = new float[3];
        Color.RGBToHSV(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor),
                hsv);
        double step = (240.0 / (double) numPieSlices);
        float baseHue = hsv[0];
        for (int i = 1; i < numPieSlices; i++) {
            float nextColorHue = ((float) (baseHue + step * ((float) i))) % ((float) 240.0);
            colors[i] = Color.HSVToColor(new float[]{nextColorHue, hsv[1], hsv[2]});
        }

        if (!adjacentColors && numPieSlices > 2) {
            int holder;
            for (int i = 0, j = numPieSlices / 2; j < numPieSlices; i += 2, j += 2) {
                // Swap
                holder = colors[i];
                colors[i] = colors[j];
                colors[j] = holder;
            }
        }
        return colors;
    }
}
