package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;

import java.sql.SQLException;

public class DatabaseContentProvider extends ContentProvider {

    DatabaseHelper databaseHelper;
    private static final String AUTHORITY = "com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider";
    public static final int TUTORIALS = 100;
    public static final int TUTORIAL_ID = 110;

    private static final String TUTORIALS_BASE_PATH = "tutorials";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TUTORIALS_BASE_PATH);

    public DatabaseContentProvider() {

        databaseHelper=DatabaseHelper.getInstance(getContext());
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        System.out.println("manu-----insert starteed.....");

        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        System.out.println("manu-----insert writable data found");
        long newID = sqlDB
                .insert(AreaPinInfoAdapter.createTable, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
            System.out.println("manu-----insert successful.....");
            return newUri;
        } else {
            System.out.println("manu-----failed to insert...." + uri);
            return uri;
        }
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
