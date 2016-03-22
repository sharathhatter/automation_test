package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.util.UIUtil;
import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchSuggestionDbHelper {

    public static final String ID = "id";
    public static final String QUERY = "query";
    public static final String TERMS = "terms";
    public static final String CATEGORIES = "categories";
    public static final String CATEGORY_URLS = "category_urls";
    public static final String SUGGESTED_TERMS = "suggested_terms";
    public static final String CREATED_ON = "created_on";
    public static final String TABLE_NAME = "searchsuggestion";
    public static final String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER " +
                    "PRIMARY KEY AUTOINCREMENT, %3$s TEXT NOT NULL, %4$s TEXT, %5$s TEXT, %6$s TEXT," +
                    "%7$s TEXT, %8$s TEXT NOT NULL);",
            TABLE_NAME, ID, QUERY, TERMS, CATEGORIES, CATEGORY_URLS,
            SUGGESTED_TERMS, CREATED_ON);

    public static final Uri CONTENT_URI = Uri.withAppendedPath(
            DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);
    public static final String MIME_TYPE_DIR =
            "vnd.android.cursor.dir/com.bigbasket.mobileapp.searchsuggestion";
    public static final String MIME_TYPE_ITEM =
            "vnd.android.cursor.item/com.bigbasket.mobileapp.searchsuggestion";

    public static void insertAsync(final Context context, final AutoSearchResponse autoSearchResponse) {
        new Thread() {
            @Override
            public void run() {
                insert(context, autoSearchResponse);
            }
        }.start();
    }

    public static void insert(Context context, AutoSearchResponse autoSearchResponse) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(QUERY, autoSearchResponse.getQuery());
        contentValues.put(CREATED_ON, new SimpleDateFormat("dd-MM-yyyy", Locale.US).
                format(new Date(System.currentTimeMillis())));

        String[] termsArray = autoSearchResponse.getTerms();
        String[] categoriesArray = autoSearchResponse.getCategories();
        String[] categoriesUrlArray = autoSearchResponse.getCategoriesUrl();
        String[] suggestedTermsArray = autoSearchResponse.getSuggestedTerm();

        if (termsArray != null && termsArray.length > 0) {
            contentValues.put(TERMS, UIUtil.strJoin(termsArray, ","));
        }
        if (categoriesArray != null && categoriesArray.length > 0) {
            contentValues.put(CATEGORIES, UIUtil.strJoin(categoriesArray, ","));
        }
        if (categoriesUrlArray != null && categoriesUrlArray.length > 0) {
            contentValues.put(CATEGORY_URLS, UIUtil.strJoin(categoriesUrlArray, ","));
        }
        if (suggestedTermsArray != null && suggestedTermsArray.length > 0) {
            contentValues.put(SUGGESTED_TERMS, UIUtil.strJoin(suggestedTermsArray, ","));
        }
        ContentResolver cr = context.getContentResolver();
        int columnsUpdated = cr.update(CONTENT_URI, contentValues, QUERY + " = ?",
                new String[]{autoSearchResponse.getQuery()});
        if(columnsUpdated <= 0) {
            cr.insert(CONTENT_URI, contentValues);
        }
    }

    public static AutoSearchResponse getStoredResponse(Context context, String query) {
        Cursor cursor = null;
        AutoSearchResponse autoSearchResponse = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, AutoSearchResponse.PROJECTION,
                    QUERY + " = ?", new String[]{query}, null);
            if (cursor != null && cursor.moveToFirst()) {
                autoSearchResponse = new AutoSearchResponse(cursor);
            }
        } catch (SQLiteException e) {
            Crashlytics.logException(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return autoSearchResponse;
    }
}
