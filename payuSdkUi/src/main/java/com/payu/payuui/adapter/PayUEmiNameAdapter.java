package com.payu.payuui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.payu.india.Model.Emi;
import com.payu.payuui.R;

import java.util.ArrayList;

public class PayUEmiNameAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Emi> mEmiList;

    public PayUEmiNameAdapter(Context context, ArrayList<Emi> emiList) {
        mContext = context;
        mEmiList = emiList;
    }

    @Override
    public int getCount() {
        if (null != mEmiList) return mEmiList.size();
        else return 0;
    }

    @Override
    public Emi getItem(int position) {
        return mEmiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PayUEmiDurationAdapter.PayUEmiVH emiViewHolder = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.payu_spinner_list_item, parent, false);
            emiViewHolder = new PayUEmiDurationAdapter.PayUEmiVH(convertView);
            convertView.setTag(emiViewHolder);
        } else {
            emiViewHolder = (PayUEmiDurationAdapter.PayUEmiVH) convertView.getTag();
        }

        Emi emi = getItem(position);

        // set text here
        emiViewHolder.emiNameTextView.setText(emi.getBankName());
        return convertView;
    }
}