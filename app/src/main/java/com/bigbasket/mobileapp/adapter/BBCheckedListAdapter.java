package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.List;

public class BBCheckedListAdapter<T> extends ArrayAdapter<T> {

    private int textColor;
    private int dp16;
    private int dp8;

    public BBCheckedListAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);

        this.textColor = context.getResources().getColor(R.color.uiv3_primary_text_color);
        this.dp16 = (int) context.getResources().getDimension(R.dimen.padding_normal);
        this.dp8 = (int) context.getResources().getDimension(R.dimen.padding_small);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            checkedTextView.setCheckMarkDrawable(R.drawable.large_radio_button);
            checkedTextView.setTextColor(textColor);
            checkedTextView.setTypeface(FontHolder.getInstance(getContext()).getFaceRobotoLight());
            checkedTextView.setPadding(dp16, dp8, dp8, dp8);
            return convertView;
        } else {
            return super.getView(position, convertView, parent);
        }
    }
}
