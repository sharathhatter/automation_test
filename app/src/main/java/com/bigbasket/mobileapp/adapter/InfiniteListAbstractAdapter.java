package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class InfiniteListAbstractAdapter<T> extends BaseAdapter {

    protected List<T> dataList;
    protected Context context;

    protected int serverListSize = -1;

    public InfiniteListAbstractAdapter(Context context, List<T> dataList, int serverListSize) {
        this.context = context;
        this.dataList = dataList;
        this.serverListSize = serverListSize;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if ((position + 1) == dataList.size() && (position + 1) < serverListSize) {
            nextPage();
        }
        return getDataView(position, convertView, parent);
    }

    public abstract View getDataView(int position, View convertVIew, ViewGroup parent);

    public abstract void nextPage();
}
