package com.bigbasket.mobileapp.fragment.account.spendTrends;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

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
}
