package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AppDataDynamicDbHelper {
    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_APP_DATA_DYNAMIC_PARAMS = "app_data_dynamic_params";

    public static final String TABLE_NAME = "app_data_dynamic";

    public static String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s " +
            "(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "%3$s TEXT);", TABLE_NAME, COLUMN_ID, COLUMN_APP_DATA_DYNAMIC_PARAMS);

    public static final Uri CONTENT_URI =
            Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);

    private Context context;

    public AppDataDynamicDbHelper(Context context) {
        this.context = context;
    }

    public void save(String appDataDynamicJson) {
        Cursor cursor = context.getContentResolver()
                .query(CONTENT_URI, new String[]{COLUMN_ID}, null, null, null);
        int existingID = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                existingID = cursor.getInt(0);
            }
            cursor.close();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_APP_DATA_DYNAMIC_PARAMS, appDataDynamicJson);
        if (existingID <= 0) {
            context.getContentResolver().insert(CONTENT_URI, contentValues);
        } else {
            context.getContentResolver().update(CONTENT_URI, contentValues,
                    COLUMN_ID + "=" + existingID, null);
        }
    }

    public static void delete(Context context) {
        context.getContentResolver().delete(CONTENT_URI, null, null);
    }

    public static String[] getDefaultProjection() {
        return new String[]{COLUMN_ID, COLUMN_APP_DATA_DYNAMIC_PARAMS};
    }
}
