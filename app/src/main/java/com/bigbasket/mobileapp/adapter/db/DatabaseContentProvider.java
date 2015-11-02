package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.application.BaseApplication;

public class DatabaseContentProvider extends ContentProvider {

    DatabaseHelper databaseHelper;
    private static final String AUTHORITY = BaseApplication.getsContext().getPackageName()+".DatabaseContentProvider";

    public static final Uri CONTENT_URI_PREFIX = Uri.parse("content://" + AUTHORITY);

    public static final int AREA_PIN_INFO = 100;
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, AreaPinInfoAdapter.tableName, AREA_PIN_INFO);
    }

    public DatabaseContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case AREA_PIN_INFO:
                long newID = sqlDB
                        .insert(AreaPinInfoAdapter.tableName, null, values);
                if (newID > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, newID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    return uri;
                }
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public boolean onCreate() {
        databaseHelper=DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();



        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case AREA_PIN_INFO:
                queryBuilder.setTables(AreaPinInfoAdapter.tableName);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(databaseHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}
