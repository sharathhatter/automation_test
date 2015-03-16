package com.bigbasket.mobileapp.adapter;

/**
 * Created by jugal on 23/12/14.
 */

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;

public class SearchViewAdapter<T> extends CursorAdapter implements Filterable {

    private T ctx;
    private LayoutInflater inflater;

    public SearchViewAdapter(T context, Cursor contactCursor) {
        super(((ActivityAware) context).getCurrentActivity(), contactCursor, false);
        this.ctx = context;
        inflater = LayoutInflater.from(((ActivityAware) context).getCurrentActivity());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtTerm = (TextView) view.findViewById(R.id.txtTerm);
        String termString = cursor.getString(1);
        if(cursor.getString(4)==null) {
            view.setBackgroundColor(((ActivityAware)context).getCurrentActivity().getResources().getColor(R.color.uiv3_tile_bkg));
            view.setClickable(false);
            view.setFocusable(false);
            view.findViewById(R.id.imgSearchListIcon).setVisibility(View.GONE);
            txtTerm.setText(termString);
        }else {
            view.setBackgroundColor(((ActivityAware)context).getCurrentActivity().getResources().getColor(R.color.white));
            view.findViewById(R.id.imgSearchListIcon).setVisibility(View.VISIBLE);
//            if(!TextUtils.isEmpty(cursor.getString(2))){
//                termString += " "+cursor.getString(2);
//            }
            txtTerm.setText(termString);
            view.setTag(cursor.getString(1));
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.search_row, parent, false);
    }
}
