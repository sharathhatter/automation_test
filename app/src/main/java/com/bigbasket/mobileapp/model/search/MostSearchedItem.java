package com.bigbasket.mobileapp.model.search;

import android.database.Cursor;

import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;

public class MostSearchedItem {
    private String query;
    private String url; // Will be null, except for categories

    public MostSearchedItem(Cursor cursor) {
        this.query = cursor.getString(cursor.getColumnIndex(MostSearchesDbHelper.COLUMN_QUERY));
        this.url = cursor.getString(cursor.getColumnIndex(MostSearchesDbHelper.COLUMN_URL));
    }

    public String getQuery() {
        return query;
    }

    public String getUrl() {
        return url;
    }
}
