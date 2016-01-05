package com.bigbasket.mobileapp.adapter.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DynamicPageDbHelper {
    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_DYNAMIC_SCREEN_TYPE = "dynamic_screen_type";
    public static final String COLUMN_SCREEN_DATA = "screen_data";
    public static final String TABLE_NAME = "dynamicScreen";

    public static String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s " +
                    "(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT , %4$s TEXT);", TABLE_NAME, COLUMN_ID,
            COLUMN_DYNAMIC_SCREEN_TYPE, COLUMN_SCREEN_DATA);

    public static final Uri CONTENT_URI =
            Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);

    private Context context;

    private static final String TIME_KEY = "_time";
    private static final String DURATION_KEY = "_duration";

    public DynamicPageDbHelper(Context context) {
        this.context = context;
    }

    public void save(String dynamicScreenType, String dynamicScreenJson, int cacheDuration) {
        Uri uri = Uri.withAppendedPath(CONTENT_URI, dynamicScreenType);
        Cursor cursor = context.getContentResolver()
                .query(uri, new String[]{COLUMN_ID},
                        COLUMN_DYNAMIC_SCREEN_TYPE + " = \'" + dynamicScreenType + "\'",
                        null, null);
        int existingID = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                existingID = cursor.getInt(0);
            }
            cursor.close();
        }
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DYNAMIC_SCREEN_TYPE, dynamicScreenType);
        cv.put(COLUMN_SCREEN_DATA, dynamicScreenJson);
        if (existingID <= 0) {
            context.getContentResolver().insert(uri, cv);
        } else {
            context.getContentResolver().update(uri,
                    cv, COLUMN_ID + " = " + existingID, null);
        }
        storeSectionRefreshData(context, dynamicScreenType, cacheDuration);
    }

    public static boolean isStale(Context context, String dynamicScreenType) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String createdOn = preferences.getString(dynamicScreenType + TIME_KEY, null);
        if (TextUtils.isEmpty(createdOn)) return false;
        int cacheDuration = preferences.getInt(dynamicScreenType + DURATION_KEY,
                Section.DEFAULT_SECTION_TIMEOUT_IN_MINUTES);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault());
        try {
            Date createOnDate = dateFormat.parse(createdOn);
            Date now = new Date();
            long minutes = TimeUnit.MINUTES.convert(now.getTime() - createOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return minutes > cacheDuration;
        } catch (ParseException e) {
            return true;
        }
    }

    public static void storeSectionRefreshData(Context context, String dynamicScreenType, int cacheDuration) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault());
        editor.putString(dynamicScreenType + TIME_KEY, dateFormat.format(new Date()));
        editor.putInt(dynamicScreenType + DURATION_KEY, cacheDuration);
        editor.apply();
    }

    public static void clearAllAsync(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new DynamicScreenResetRunnable(context));
        } else {
            new Thread(new DynamicScreenResetRunnable(context)).start();
        }
    }

    public static void clearAll(Context context) {
        for (String dynamicScreenType : new String[]{AbstractDynamicPageSyncService.HOME_PAGE, AbstractDynamicPageSyncService.MAIN_MENU}) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI, dynamicScreenType);
            context.getContentResolver().delete(uri, null, null);
        }
    }

    private static class DynamicScreenResetRunnable implements Runnable {
        private Context context; // Hard reference is needed

        public DynamicScreenResetRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            clearAll(context);
        }
    }

    public static String[] getDefaultProjection() {
        return new String[]{COLUMN_ID, COLUMN_DYNAMIC_SCREEN_TYPE, COLUMN_SCREEN_DATA};
    }
}
