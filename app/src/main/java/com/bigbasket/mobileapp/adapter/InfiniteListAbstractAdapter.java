package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.bigbasket.mobileapp.R;

import java.util.List;

public abstract class InfiniteListAbstractAdapter<T> extends BaseAdapter {

    protected List<T> dataList;
    protected Context context;

    protected int serverListSize = -1;

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_DATA = 1;

    public InfiniteListAbstractAdapter(Context context, List<T> dataList, int serverListSize) {
        this.context = context;
        this.dataList = dataList;
        this.serverListSize = serverListSize;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_DATA;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return dataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position >= dataList.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public T getItem(int position) {
        return getItemViewType(position) == VIEW_TYPE_DATA ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return getItemViewType(position) == VIEW_TYPE_DATA ? position : -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == VIEW_TYPE_LOADING) {
            nextPage();
            return getFooterView(position, convertView, parent);
        }
        return getDataView(position, convertView, parent);
    }

    public abstract View getDataView(int position, View convertVIew, ViewGroup parent);

    public View getFooterView(int position, View convertView, ViewGroup parent) {
        if (position >= serverListSize && serverListSize > 0) {
            return new View(context);
        }
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_list_loading_footer, parent, false);
        }
        return row;
    }

    public abstract void nextPage();
}
