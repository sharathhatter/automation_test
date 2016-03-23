package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

public class MostSearchesDbHelper {

    public static final String ID = "_id";
    public static final String QUERY = "query";
    public static final String CATEGORY_URL = "category_url";
    public static final String COUNT = "count";
    public static final String TABLE_NAME = "mostsearches";
    public static final String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s TEXT NOT NULL, %4$s TEXT, %5$s INTEGER NOT NULL);",
            TABLE_NAME, ID, QUERY, CATEGORY_URL, COUNT);

    public static final Uri CONTENT_URI = Uri.withAppendedPath(
            DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);
    public static final String MIME_TYPE_DIR =
            "vnd.android.cursor.dir/com.bigbasket.mobileapp.mostsearches";
    public static final String MIME_TYPE_ITEM =
            "vnd.android.cursor.item/com.bigbasket.mobileapp.mostsearches";

    public static List<MostSearchedItem> getRecentSearchedItems(Context context, int limit) {
        Cursor cursor = null;
        List<MostSearchedItem> mostSearchedItems = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI,
                    MostSearchedItem.PROJECTION,
                    null, null, ID + " DESC LIMIT " + String.valueOf(limit));
            if (cursor != null && cursor.moveToFirst()) {
                mostSearchedItems = new ArrayList<>(cursor.getCount());
                do {
                    mostSearchedItems.add(new MostSearchedItem(cursor));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            //e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return mostSearchedItems;
    }

    public static void update(Context context, String query) {
        update(context, query, null);
    }

    public static void update(Context context, String query, String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(QUERY, query);
        if (!TextUtils.isEmpty(url)) {
            contentValues.put(CATEGORY_URL, url);
        }
        ContentResolver cr = context.getContentResolver();
        int count = cr.update(CONTENT_URI, contentValues,
                    QUERY + " = ?", new String[]{query});
        if(count <= 0) {
            contentValues.put(COUNT, 1);
            cr.insert(CONTENT_URI, contentValues);
        }
    }

    public static void deleteTerm(Context context, String term) {
        context.getContentResolver().delete(CONTENT_URI, QUERY + " = ?", new String[]{term});
    }

}
