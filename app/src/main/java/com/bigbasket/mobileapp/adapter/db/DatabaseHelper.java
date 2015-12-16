package com.bigbasket.mobileapp.adapter.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.bigbasket.mobileapp.contentProvider.SectionItemAnalyticsData;
import com.crashlytics.android.Crashlytics;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bigbasket.db";
    /**
     * Version 16: Added "app_data_dynamic" table
     * Version 17: Fix crashes
     * Version 18: Added section_item_analytics_data table
     */
    protected static final int DATABASE_VERSION = 18;
    public static SQLiteDatabase db = null;
    private static volatile DatabaseHelper dbAdapter = null;
    private static boolean isConnectionOpen = false;
    private static final Object lock = new Object();

    private DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DatabaseHelper getInstance(Context context) {
        DatabaseHelper helper = dbAdapter;
        if (helper == null) {
            synchronized (lock) {
                helper = dbAdapter;
                if (helper == null) {
                    helper = new DatabaseHelper(context.getApplicationContext(),
                            DATABASE_NAME, null, DATABASE_VERSION);
                    dbAdapter = helper;
                }
            }
        }
        return dbAdapter;
    }

    public void open(Context context) {
        if (!isConnectionOpen) {
            try {
                db = getInstance(context).getWritableDatabase();
            } catch (SQLiteException e) {
                db = getInstance(context).getReadableDatabase();
            }
            isConnectionOpen = true;
        }
    }

    public void close() {
        if (isConnectionOpen) {
            db.close();
            isConnectionOpen = false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 15) {
            try {
                db.execSQL("DELETE FROM " + CategoryAdapter.TABLE_NAME);
                db.execSQL("DROP TABLE " + CategoryAdapter.TABLE_NAME);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            try {
                db.execSQL("DELETE FROM " + SubCategoryAdapter.TABLE_NAME);
                db.execSQL("DROP TABLE " + SubCategoryAdapter.TABLE_NAME);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            try {
                db.execSQL("DELETE FROM " + AreaPinInfoDbHelper.TABLE_NAME);
                db.execSQL("DROP TABLE " + AreaPinInfoDbHelper.TABLE_NAME);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        if (oldVersion < 16) {
            upgradeTo16(db);
        }
        if (oldVersion < 17) {
            upgradeTo17(db);
        }

        if (oldVersion < 18) {
            upgradeTo18(db);
        }
    }

    private void upgradeTo16(SQLiteDatabase db) {
        db.execSQL(DynamicPageDbHelper.CREATE_TABLE);
        db.execSQL(AppDataDynamicDbHelper.CREATE_TABLE);
        db.execSQL(CategoryAdapter.CREATE_TABLE);
        db.execSQL(SubCategoryAdapter.CREATE_TABLE);
        db.execSQL(AreaPinInfoDbHelper.CREATE_TABLE);
    }

    private void upgradeTo17(SQLiteDatabase db) {
        try {
            db.execSQL(SearchSuggestionDbHelper.CREATE_TABLE);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        try {
            db.execSQL(MostSearchesDbHelper.CREATE_TABLE);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void upgradeTo18(SQLiteDatabase db) {
        try {
            createSectionItemAnalyticsTable(db);
        }  catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(CategoryAdapter.CREATE_TABLE);
        db.execSQL(SubCategoryAdapter.CREATE_TABLE);
        db.execSQL(AreaPinInfoDbHelper.CREATE_TABLE);
        db.execSQL(SearchSuggestionDbHelper.CREATE_TABLE);
        db.execSQL(MostSearchesDbHelper.CREATE_TABLE);
        db.execSQL(DynamicPageDbHelper.CREATE_TABLE);  // Added in version 16
        db.execSQL(AppDataDynamicDbHelper.CREATE_TABLE);  // Added in version 16
        createSectionItemAnalyticsTable(db);
    }

    private void createSectionItemAnalyticsTable(SQLiteDatabase db) {
        String createStmt = "CREATE TABLE IF NOT EXISTS " + SectionItemAnalyticsData.TABLE_NAME
                + " ( " + SectionItemAnalyticsData.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SectionItemAnalyticsData.SECTION_ID + " TEXT, "
                + SectionItemAnalyticsData.CITY_ID + " TEXT, "
                + SectionItemAnalyticsData.CLICKS + " INTEGER, "
                + SectionItemAnalyticsData.IMPRESSIONS + " INTEGER, "
                + SectionItemAnalyticsData.ANALYTICS_ATTRS + " TEXT );";
        db.execSQL(createStmt);
    }
}