package com.bigbasket.mobileapp.adapter.location;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Filter;

import com.bigbasket.mobileapp.interfaces.PlaceAutoSuggestListener;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;

import java.util.List;

public class PlaceAutoSuggestAdapter<T> extends BBArrayAdapter<T> {

    public PlaceAutoSuggestAdapter(Context context, int resource, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, typeface, itemColor, dropDownItemColor);
    }

    public PlaceAutoSuggestAdapter(Context context, int resource, int textViewResourceId, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId, typeface, itemColor, dropDownItemColor);
    }

    public PlaceAutoSuggestAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public PlaceAutoSuggestAdapter(Context context, int resource, int textViewResourceId, T[] objects, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId, objects, typeface, itemColor, dropDownItemColor);
    }

    public PlaceAutoSuggestAdapter(Context context, int resource, List<T> objects, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, objects, typeface, itemColor, dropDownItemColor);
    }

    public PlaceAutoSuggestAdapter(Context context, int resource, int textViewResourceId, List<T> objects, Typeface typeface, int itemColor, int dropDownItemColor) {
        super(context, resource, textViewResourceId, objects, typeface, itemColor, dropDownItemColor);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                clear();
                ((PlaceAutoSuggestListener) getContext()).displaySuggestion(constraint.toString());
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }
}
