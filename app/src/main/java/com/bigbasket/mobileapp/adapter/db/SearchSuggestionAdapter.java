package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchSuggestionAdapter {

    private Context context;

    public SearchSuggestionAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUERY = "query";
    public static final String COLUMN_TERMS = "terms";
    public static final String COLUMN_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_URLS = "category_urls";
    public static final String COLUMN_SUGGESTED_TERMS = "suggested_terms";
    public static final String COLUMN_CREATED_ON = "created_on";
    public static final String tableName = "searchsuggestion";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER " +
                    "PRIMARY KEY AUTOINCREMENT, %3$s TEXT NOT NULL, %4$s TEXT, %5$s TEXT, %6$s TEXT," +
                    "%7$s TEXT, %8$s TEXT NOT NULL);",
            tableName, COLUMN_ID, COLUMN_QUERY, COLUMN_TERMS, COLUMN_CATEGORIES, COLUMN_CATEGORY_URLS,
            COLUMN_SUGGESTED_TERMS, COLUMN_CREATED_ON);


    public void insert(AutoSearchResponse autoSearchResponse) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUERY, DatabaseUtils.sqlEscapeString(autoSearchResponse.getQuery()));
        contentValues.put(COLUMN_CREATED_ON, new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis())));
        if (!ArrayUtils.isEmpty(autoSearchResponse.getTerms())) {
            contentValues.put(COLUMN_TERMS, StringUtils.join(autoSearchResponse.getTerms(), ','));
        }
        if (!ArrayUtils.isEmpty(autoSearchResponse.getCategories())) {
            contentValues.put(COLUMN_CATEGORIES, StringUtils.join(autoSearchResponse.getCategories(), ','));
        }
        if (!ArrayUtils.isEmpty(autoSearchResponse.getCategoriesUrl())) {
            contentValues.put(COLUMN_CATEGORY_URLS, StringUtils.join(autoSearchResponse.getCategoriesUrl(), ','));
        }
        if (!ArrayUtils.isEmpty(autoSearchResponse.getSuggestedTerm())) {
            contentValues.put(COLUMN_SUGGESTED_TERMS, StringUtils.join(autoSearchResponse.getSuggestedTerm(), ','));
        }
        if (hasElement(autoSearchResponse.getQuery(), COLUMN_QUERY)) {
            DatabaseHelper.db.update(tableName, contentValues,
                    COLUMN_QUERY + " = \"" + DatabaseUtils.sqlEscapeString(autoSearchResponse.getQuery()) + "\"", null);
        } else {
            DatabaseHelper.db.insert(tableName, null, contentValues);
        }
    }

    public AutoSearchResponse getStoredResponse(String query) {
        Cursor cursor = null;
        AutoSearchResponse autoSearchResponse = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_QUERY, COLUMN_CREATED_ON, COLUMN_TERMS,
                            COLUMN_CATEGORIES, COLUMN_CATEGORY_URLS, COLUMN_SUGGESTED_TERMS},
                    COLUMN_QUERY + " = \"" + DatabaseUtils.sqlEscapeString(query) + "\"", null, null, null, COLUMN_ID);
            if (cursor.moveToFirst()) {
                autoSearchResponse = new AutoSearchResponse(cursor);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return autoSearchResponse;
    }

    public void delete(AutoSearchResponse autoSearchResponse) {
        DatabaseHelper.db.delete(tableName, COLUMN_QUERY + " = \"" + autoSearchResponse.getQuery() + "\"", null);
    }

    public boolean hasElement(String value, String columnName) {
        Cursor cursor = null;
        boolean result = false;
        try {
            cursor = DatabaseHelper.db.query(true, tableName, null,
                    columnName + "= \"" + DatabaseUtils.sqlEscapeString(value) + "\"", null, null, null, null, null);
            result = cursor.moveToFirst();
        } catch (SQLException ex) {
            ex.printStackTrace();
            result = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }
}
