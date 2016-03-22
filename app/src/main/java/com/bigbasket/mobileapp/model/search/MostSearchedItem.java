package com.bigbasket.mobileapp.model.search;

import android.database.Cursor;

import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;

public class MostSearchedItem {
    private String query;
    private String url; // Will be null, except for categories

    public static final String[] PROJECTION = new String[]{
            MostSearchesDbHelper.QUERY,
            MostSearchesDbHelper.CATEGORY_URL};

    private static final int COLUMN_QUERY_INDEX = 0;
    private static final int COLUMN_CATEGORY_URL_INDEX = 1;

    public MostSearchedItem(Cursor cursor) {
        this.query = cursor.getString(COLUMN_QUERY_INDEX);
        this.url = cursor.getString(COLUMN_CATEGORY_URL_INDEX);
    }

    public String getQuery() {
        return query;
    }

    public String getUrl() {
        return url;
    }
}
