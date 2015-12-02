package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;

public class DatabaseContentProvider extends ContentProvider {
    DatabaseHelper databaseHelper;
    private static final String TAG = DatabaseContentProvider.class.getName();

    public static final String AUTHORITY = BaseApplication.getsContext().getPackageName() + ".DatabaseContentProvider";

    public static final Uri CONTENT_URI_PREFIX = Uri.parse("content://" + AUTHORITY);

    public static final int AREA_PIN_INFO_URI_MATCHER_CODE = 100;
    public static final int HOME_SECTION_URI_MATCHER_CODE = 200;
    public static final int MAIN_MENU_SECTION_URI_MATCHER_CODE = 300;
    public static final int APP_DATA_DYNAMIC_URI_MATCHER_CODE = 400;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, AreaPinInfoDbHelper.TABLE_NAME, AREA_PIN_INFO_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, DynamicPageDbHelper.TABLE_NAME + "/" + AbstractDynamicPageSyncService.MAIN_MENU,
                MAIN_MENU_SECTION_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, DynamicPageDbHelper.TABLE_NAME + "/" + AbstractDynamicPageSyncService.HOME_PAGE,
                HOME_SECTION_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, AppDataDynamicDbHelper.TABLE_NAME, APP_DATA_DYNAMIC_URI_MATCHER_CODE);
    }

    public DatabaseContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        Log.d(TAG, "Running delete for uri = " + uri);
        int rowCount = sqlDB.delete(tableName, selection, selectionArgs);
        notifyChange(uri);
        return rowCount;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        Log.d(TAG, "Running insert for uri = " + uri + " and values = " + values);
        long newID = sqlDB.insert(tableName, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return newUri;
        }
        return uri;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String tableName = getTableName(uri);
        queryBuilder.setTables(tableName);
        Log.d(TAG, "Running query for uri = " + uri);
        Cursor cursor = queryBuilder.query(databaseHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String tableName = getTableName(uri);
        Log.d(TAG, "Running update for uri = " + uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowCount = db.update(tableName, values, selection, selectionArgs);
        if (rowCount > 0 && getContext() != null) {
            notifyChange(uri);
        }
        return rowCount;
    }

    private String getTableName(Uri uri) throws IllegalArgumentException {
        switch (sURIMatcher.match(uri)) {
            case AREA_PIN_INFO_URI_MATCHER_CODE:
                return AreaPinInfoDbHelper.TABLE_NAME;
            case HOME_SECTION_URI_MATCHER_CODE:
            case MAIN_MENU_SECTION_URI_MATCHER_CODE:
                return DynamicPageDbHelper.TABLE_NAME;
            case APP_DATA_DYNAMIC_URI_MATCHER_CODE:
                return AppDataDynamicDbHelper.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private void notifyChange(@NonNull Uri uri) {
        Context context = getContext();
        if (context == null) {
            context = BaseApplication.getsContext();
        }
        context.getContentResolver().notifyChange(uri, null);
    }
}
