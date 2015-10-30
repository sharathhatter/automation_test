package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;

import java.sql.SQLException;

public class DatabaseContentProvider extends ContentProvider {

    DatabaseHelper databaseHelper;
    private static final String AUTHORITY = "com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider";
    public static final Uri CONTENT_URI_PREFIX = Uri.parse("content://" + AUTHORITY);

    public DatabaseContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();

        long newID = sqlDB
                .insert(AreaPinInfoAdapter.tableName, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
            return newUri;
        } else {
            System.out.println("failed to insert...." + uri);
            return uri;
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
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}
