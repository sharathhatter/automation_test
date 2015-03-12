package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.search.MostSearchedItem;

import java.util.ArrayList;
import java.util.List;

public class MostSearchesAdapter {

    private Context context;

    public MostSearchesAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUERY = "query";
    public static final String COLUMN_URL = "category_url";
    public static final String COLUMN_COUNT = "count";
    public static final String tableName = "mostsearches";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s TEXT NOT NULL, %4$s TEXT, %5$s INTEGER NOT NULL);",
            tableName, COLUMN_ID, COLUMN_QUERY, COLUMN_URL, COLUMN_COUNT);

    public List<MostSearchedItem> getMostSearchedItems(int limit) {
        Cursor cursor = null;
        List<MostSearchedItem> mostSearchedItems = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_QUERY, COLUMN_URL, COLUMN_COUNT},
                    null, null, null, null, COLUMN_COUNT + " ASC", String.valueOf(limit));
            if (cursor.moveToFirst()) {
                mostSearchedItems = new ArrayList<>();
                do {
                    mostSearchedItems.add(new MostSearchedItem(cursor));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return mostSearchedItems;
    }

    public List<MostSearchedItem> getRecentSearchedItems(int limit) {
        Cursor cursor = null;
        List<MostSearchedItem> mostSearchedItems = null;
        try {
            cursor = DatabaseHelper.db.rawQuery("SELECT * FROM "+ tableName+ " ORDER BY id DESC LIMIT "+ limit, null);
            if (cursor.moveToFirst()) {
                mostSearchedItems = new ArrayList<>();
                do {
                    mostSearchedItems.add(new MostSearchedItem(cursor));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return mostSearchedItems;
    }

    /*
    SELECT * FROM (
    SELECT * FROM table ORDER BY id DESC LIMIT 50
) sub
ORDER BY id ASC

This will select the last 50 rows from table, and then order them in ascending order.
     */

    public int getCountForQuery(String query) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_COUNT},
                    COLUMN_QUERY + "=" + DatabaseUtils.sqlEscapeString(query),
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return count;
    }

    public int getRowCount(){
        return DatabaseHelper.db.rawQuery("select * from "+tableName,null).getCount();
    }

    public void update(String query) {
        update(query, null);
    }

    public void update(String query, String url) {
        int currentCount = getCountForQuery(query);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUERY, query);
        if (!TextUtils.isEmpty(url)) {
            contentValues.put(COLUMN_URL, url);
        }
        contentValues.put(COLUMN_COUNT, currentCount + 1);
        if (currentCount > 0) {
            DatabaseHelper.db.update(tableName, contentValues,
                    COLUMN_QUERY + " = \"" + DatabaseUtils.sqlEscapeString(query) + "\"", null);
        } else {
            DatabaseHelper.db.insert(tableName, null, contentValues);
        }
    }

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }
}
