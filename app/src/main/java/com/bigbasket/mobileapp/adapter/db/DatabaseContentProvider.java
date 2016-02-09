package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.contentProvider.SectionItemAnalyticsData;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.crashlytics.android.Crashlytics;

public class DatabaseContentProvider extends ContentProvider {
    DatabaseHelper databaseHelper;
    private static final String TAG = DatabaseContentProvider.class.getName();

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".bbprovider";

    public static final Uri CONTENT_URI_PREFIX = Uri.parse("content://" + AUTHORITY);

    public static final int AREA_PIN_INFO_URI_MATCHER_CODE = 100;
    public static final int HOME_SECTION_URI_MATCHER_CODE = 101;
    public static final int MAIN_MENU_SECTION_URI_MATCHER_CODE = 102;
    public static final int APP_DATA_DYNAMIC_URI_MATCHER_CODE = 103;
    public static final int SECTION_ITEM_ANALYTICS_DATA_DIR = 104;
    public static final int SECTION_ITEM_ANALYTICS_DATA_ITEM = 105;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, AreaPinInfoDbHelper.TABLE_NAME, AREA_PIN_INFO_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, DynamicPageDbHelper.TABLE_NAME + "/" + AbstractDynamicPageSyncService.MAIN_MENU,
                MAIN_MENU_SECTION_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, DynamicPageDbHelper.TABLE_NAME + "/" + AbstractDynamicPageSyncService.HOME_PAGE,
                HOME_SECTION_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, AppDataDynamicDbHelper.TABLE_NAME, APP_DATA_DYNAMIC_URI_MATCHER_CODE);
        sURIMatcher.addURI(AUTHORITY, SectionItemAnalyticsData.TABLE_NAME,
                SECTION_ITEM_ANALYTICS_DATA_DIR);
        sURIMatcher.addURI(AUTHORITY, SectionItemAnalyticsData.TABLE_NAME + "/#",
                SECTION_ITEM_ANALYTICS_DATA_ITEM);
    }

    public DatabaseContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        switch (sURIMatcher.match(uri)) {
            case SECTION_ITEM_ANALYTICS_DATA_ITEM:
                String id = uri.getLastPathSegment();
                String idSelection = SectionItemAnalyticsData.ID + " = '" + id + "'";
                if (selection == null) {
                    selection = idSelection;
                } else {
                    selection += " AND " + idSelection;
                }
                break;
        }
        int rowCount;
        sqlDB.beginTransaction();
        try {
            rowCount = sqlDB.delete(tableName, selection, selectionArgs);
            sqlDB.setTransactionSuccessful();
        } finally {
            sqlDB.endTransaction();
        }
        notifyChange(uri);
        return rowCount;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SECTION_ITEM_ANALYTICS_DATA_DIR:
                return SectionItemAnalyticsData.MIME_TYPE_DIR;
            case SECTION_ITEM_ANALYTICS_DATA_ITEM:
                return SectionItemAnalyticsData.MIME_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        long newID;
        sqlDB.beginTransaction();
        try {
            newID = sqlDB.insert(tableName, null, values);
            sqlDB.setTransactionSuccessful();
        } finally {
            sqlDB.endTransaction();
        }
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            notifyChange(uri);
            return newUri;
        } else {
            return null;
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        Log.d(TAG, "Running bulkInsert for uri = " + uri);
        int insertedCount = 0;
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        sqlDB.beginTransaction();
        try {
            for (ContentValues cv : values) {
                sqlDB.insertOrThrow(tableName, null, cv);
                insertedCount++;
            }
            sqlDB.setTransactionSuccessful();
        } catch (SQLException e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Failed bulkInsert for uri = " + uri, e);
        } finally {
            sqlDB.endTransaction();
        }
        return insertedCount;
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
        switch (sURIMatcher.match(uri)) {
            case SECTION_ITEM_ANALYTICS_DATA_ITEM:
                String id = uri.getLastPathSegment();
                String idSelection = SectionItemAnalyticsData.ID + " = '" + id + "'";
                if (selection == null) {
                    selection = idSelection;
                } else {
                    selection += " AND " + idSelection;
                }
                break;
        }
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
        switch (sURIMatcher.match(uri)) {
            case SECTION_ITEM_ANALYTICS_DATA_ITEM:
                String id = uri.getLastPathSegment();
                String idSelection = SectionItemAnalyticsData.ID + " = '" + id + "'";
                if (selection == null) {
                    selection = idSelection;
                } else {
                    selection += " AND " + idSelection;
                }
                break;
        }
        db.beginTransaction();
        int rowCount;
        try {
            rowCount = db.update(tableName, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowCount > 0) {
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
            case SECTION_ITEM_ANALYTICS_DATA_DIR:
            case SECTION_ITEM_ANALYTICS_DATA_ITEM:
                return SectionItemAnalyticsData.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private void notifyChange(@NonNull Uri uri) {
        Context context = getContext();
        if (context == null) {
            context = BaseApplication.getContext();
        }
        context.getContentResolver().notifyChange(uri, null);
    }
}
