package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;

import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.util.UIUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchSuggestionAdapter {

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
    private Context context;

    public SearchSuggestionAdapter(Context context) {
        this.context = context;
        open();
    }

    public void insertAsync(final AutoSearchResponse autoSearchResponse) {
        new Thread() {
            @Override
            public void run() {
                insert(autoSearchResponse);
            }
        }.start();
    }

    public void insert(AutoSearchResponse autoSearchResponse) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUERY, DatabaseUtils.sqlEscapeString(autoSearchResponse.getQuery()));
        contentValues.put(COLUMN_CREATED_ON, new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).
                format(new Date(System.currentTimeMillis())));

        String[] termsArray = autoSearchResponse.getTerms();
        String[] categoriesArray = autoSearchResponse.getCategories();
        String[] categoriesUrlArray = autoSearchResponse.getCategoriesUrl();
        String[] suggestedTermsArray = autoSearchResponse.getSuggestedTerm();

        if (termsArray != null && termsArray.length > 0) {
            contentValues.put(COLUMN_TERMS, UIUtil.strJoin(termsArray, ","));
        }
        if (categoriesArray != null && categoriesArray.length > 0) {
            contentValues.put(COLUMN_CATEGORIES, UIUtil.strJoin(categoriesArray, ","));
        }
        if (categoriesUrlArray != null && categoriesUrlArray.length > 0) {
            contentValues.put(COLUMN_CATEGORY_URLS, UIUtil.strJoin(categoriesUrlArray, ","));
        }
        if (suggestedTermsArray != null && suggestedTermsArray.length > 0) {
            contentValues.put(COLUMN_SUGGESTED_TERMS, UIUtil.strJoin(suggestedTermsArray, ","));
        }
        if (hasElement(autoSearchResponse.getQuery(), COLUMN_QUERY)) {
            DatabaseHelper.db.update(tableName, contentValues,
                    COLUMN_QUERY + " = " + DatabaseUtils.sqlEscapeString(autoSearchResponse.getQuery()), null);
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
                    COLUMN_QUERY + " = " + DatabaseUtils.sqlEscapeString(query), null, null, null, COLUMN_ID);
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

    public boolean hasElement(String value, String columnName) {
        Cursor cursor = null;
        boolean result = false;
        try {
            cursor = DatabaseHelper.db.query(true, tableName, null,
                    columnName + " = " + DatabaseUtils.sqlEscapeString(value), null, null, null, null, null);
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
