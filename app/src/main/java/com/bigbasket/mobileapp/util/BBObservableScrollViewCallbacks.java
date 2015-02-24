package com.bigbasket.mobileapp.util;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

public abstract class BBObservableScrollViewCallbacks implements ObservableScrollViewCallbacks {

    private boolean fitsScreen;
    private RecyclerView recyclerView;
    private ListView listView;

    protected BBObservableScrollViewCallbacks(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    protected BBObservableScrollViewCallbacks(@NonNull ListView listView) {
        this.listView = listView;
    }

    public abstract void showItem();

    public abstract void hideItem();

    @Override
    public void onDownMotionEvent() {
        Handler handler = new Handler();

        if (recyclerView != null) {
            handler.post(new RecyclerViewObserver(recyclerView));
        } else if (listView != null) {
            handler.post(new ListViewObserver(listView));
        }
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (fitsScreen) {
                showItem();
            } else {
                hideItem();
            }
        } else if (scrollState == ScrollState.DOWN) {
            showItem();
        }
    }

    private class RecyclerViewObserver implements Runnable {
        private RecyclerView recyclerView;

        private RecyclerViewObserver(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void run() {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int last = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                fitsScreen = last == layoutManager.getChildCount() - 1 && layoutManager.getChildAt(last).getBottom() <= layoutManager.getHeight();
            } else {
                fitsScreen = false;
            }
        }
    }

    private class ListViewObserver implements Runnable {
        private ListView listView;

        private ListViewObserver(ListView listView) {
            this.listView = listView;
        }

        @Override
        public void run() {
            int last = listView.getLastVisiblePosition();
            fitsScreen = last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= listView.getHeight();
        }
    }
}
