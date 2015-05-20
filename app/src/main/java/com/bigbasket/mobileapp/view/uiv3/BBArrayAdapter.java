package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

public class BBArrayAdapter<T> extends ArrayAdapter<T> {
    private Typeface typeface;
    private int itemColor;
    private int dropDownItemColor;

    public BBArrayAdapter(Context context, int resource, Typeface typeface,
                          int itemColor, int dropDownItemColor) {
        super(context, resource);
        this.typeface = typeface;
        this.itemColor = itemColor;
        this.dropDownItemColor = dropDownItemColor;
    }

    public BBArrayAdapter(Context context, int resource, int textViewResourceId,
                          Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId);
        this.typeface = typeface;
        this.itemColor = itemColor;
        this.dropDownItemColor = dropDownItemColor;
    }

    public BBArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public BBArrayAdapter(Context context, int resource, int textViewResourceId,
                          T[] objects, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId, objects);
        this.typeface = typeface;
        this.itemColor = itemColor;
        this.dropDownItemColor = dropDownItemColor;
    }

    public BBArrayAdapter(Context context, int resource, List<T> objects, Typeface typeface,
                          int itemColor, int dropDownItemColor) {
        super(context, resource, objects);
        this.typeface = typeface;
        this.itemColor = itemColor;
        this.dropDownItemColor = dropDownItemColor;
    }

    public BBArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects,
                          Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId, objects);
        this.typeface = typeface;
        this.itemColor = itemColor;
        this.dropDownItemColor = dropDownItemColor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(typeface);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            ((TextView) view).setTextColor(itemColor);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        if (view instanceof CheckedTextView) {
            ((CheckedTextView) view).setTypeface(typeface);
            ((CheckedTextView) view).setTextColor(dropDownItemColor);
        } else if (view instanceof TextView) {
            ((TextView) view).setTypeface(typeface);
            ((TextView) view).setTextColor(dropDownItemColor);
        }
        return view;
    }
}
