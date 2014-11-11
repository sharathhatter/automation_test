package com.bigbasket.mobileapp.model.search;

import android.database.Cursor;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;

public class MostSearchedItem {
    private String query;
    private String url; // Will be null, except for categories

    public MostSearchedItem(Cursor cursor) {
        this.query = cursor.getString(cursor.getColumnIndex(MostSearchesAdapter.COLUMN_QUERY));
        this.url = cursor.getString(cursor.getColumnIndex(MostSearchesAdapter.COLUMN_URL));
    }

    public String getQuery() {
        return query;
    }

    public String getUrl() {
        return url;
    }
}
