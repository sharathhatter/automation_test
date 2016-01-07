package com.payu.payuui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.payu.india.Model.Emi;
import com.payu.payuui.R;

import java.util.ArrayList;

public class PayUEmiDurationAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Emi> mEmiList;
    Emi mEmi;
    ArrayList<Emi> mSelectedEmiList;

    public PayUEmiDurationAdapter(Context context, ArrayList<Emi> emiList, Emi emi) {
        mContext = context;
        mEmiList = emiList;
        mEmi = emi;
        mSelectedEmiList = null;
        mSelectedEmiList = new ArrayList<>();
        for (int i = 0; i < emiList.size(); i++) {
            if (emiList.get(i).getBankName().contentEquals(emi.getBankName())) { // we found the current bank and bank is common in the list
                mSelectedEmiList.add(emiList.get(i));
            }
        }
    }

    @Override
    public int getCount() {
        if (null != mSelectedEmiList) return mSelectedEmiList.size();
        else return 0;
    }

    @Override
    public Emi getItem(int position) {
        return mSelectedEmiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PayUEmiVH emiViewHolder = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.payu_spinner_list_item, parent, false);
            emiViewHolder = new PayUEmiVH(convertView);
            convertView.setTag(emiViewHolder);
        } else {
            emiViewHolder = (PayUEmiVH) convertView.getTag();
        }

        Emi emi = getItem(position);
        // set text here
        emiViewHolder.emiNameTextView.setText(emi.getBankTitle());
        return convertView;
    }

    public static class PayUEmiVH {
        TextView emiNameTextView;

        PayUEmiVH(View view) {
            emiNameTextView = (TextView) view.findViewById(R.id.text_view_spinner_item);
        }
    }
}