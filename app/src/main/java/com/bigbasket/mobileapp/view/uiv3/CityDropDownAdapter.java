package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.List;

public class CityDropDownAdapter<T> extends ArrayAdapter<T> {
    public CityDropDownAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CityDropDownAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CityDropDownAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public CityDropDownAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CityDropDownAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public CityDropDownAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_transparent_spinner_view, parent, false);
        }
        TextView txtRow = (TextView) row.findViewById(R.id.txtRow);
        txtRow.setTypeface(FontHolder.getInstance(getContext()).getFaceRobotoRegular());
        txtRow.setText(getItem(position).toString());
        return row;
    }
}
