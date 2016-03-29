package com.bigbasket.mobileapp.adapter.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.bigbasket.mobileapp.contentProvider.SectionItemAnalyticsData;
import com.bigbasket.mobileapp.model.product.Category;
import com.bigbasket.mobileapp.util.CompressUtil;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bigbasket.db";
    /**
     * Version 16: Added "app_data_dynamic" table
     * Version 17: Fix crashes
     * Version 18: Added section_item_analytics_data table
     * Version 19: Converted section_data from TEXT to BLOB
     * Version 20: Added 'date' column sectionItemAnalyticsTable
     * Version 21: Updated triggers for mostsearches table and remove area pin info table
     */
    protected static final int DATABASE_VERSION = 21;
    private static volatile DatabaseHelper dbAdapter = null;
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        initializeDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 15) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + CategoryAdapter.TABLE_NAME);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            try {
                db.execSQL("DROP TABLE IF EXISTS " + SubCategoryAdapter.TABLE_NAME);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            try {
                db.execSQL("DROP TABLE IF EXISTS " + AreaPinInfoDbHelper.TABLE_NAME);
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
        if (oldVersion < 19) {
            upgradeTo19(db);
        }
        if (oldVersion < 20) {
            upgradeTo20(db);
        }
        if (oldVersion < 21) {
            upgradeTo21(db);
        }
    }
    
    private void upgradeTo16(SQLiteDatabase db) {
        db.execSQL(DynamicPageDbHelper.CREATE_TABLE);
        db.execSQL(AppDataDynamicDbHelper.CREATE_TABLE);
        db.execSQL(SubCategoryAdapter.CREATE_TABLE);
    }

    private void upgradeTo17(SQLiteDatabase db) {
        db.execSQL(SearchSuggestionDbHelper.CREATE_TABLE);
        db.execSQL(MostSearchesDbHelper.CREATE_TABLE);
    }

    private void upgradeTo18(SQLiteDatabase db) {
        createSectionItemAnalyticsTable(db);
    }

    private void upgradeTo19(SQLiteDatabase db) {
        ArrayList<DynamicPageDbHelper.DynamicPageDataHolder> sectionsToCompressList = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DynamicPageDbHelper.TABLE_NAME,
                    DynamicPageDbHelper.getDefaultProjection(), null, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        sectionsToCompressList = new ArrayList<>();
                        do {
                            DynamicPageDbHelper.DynamicPageDataHolder holder =
                                    new DynamicPageDbHelper.DynamicPageDataHolder(cursor);
                            sectionsToCompressList.add(holder);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    cursor.close();
                }
            }
            db.execSQL("DROP TABLE IF EXISTS " + DynamicPageDbHelper.TABLE_NAME);
            db.execSQL(DynamicPageDbHelper.CREATE_TABLE);
            if (sectionsToCompressList != null && sectionsToCompressList.size() > 0) {
                for (DynamicPageDbHelper.DynamicPageDataHolder holder : sectionsToCompressList) {
                    ContentValues cv = new ContentValues();
                    cv.put(DynamicPageDbHelper.COLUMN_ID, holder.getId());
                    cv.put(DynamicPageDbHelper.COLUMN_DYNAMIC_SCREEN_TYPE, holder.getDynamicScreenType());
                    cv.put(DynamicPageDbHelper.COLUMN_SCREEN_DATA, TextUtils.isEmpty(holder.getDynamicScreenData()) ? null :
                            CompressUtil.gzipCompress(holder.getDynamicScreenData()));
                    db.insert(DynamicPageDbHelper.TABLE_NAME, null, cv);
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            throw new RuntimeException(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void upgradeTo20(SQLiteDatabase db) {
        if(!isColumnExists(db, SectionItemAnalyticsData.TABLE_NAME, SectionItemAnalyticsData.DATE)) {
            db.execSQL("ALTER TABLE " + SectionItemAnalyticsData.TABLE_NAME
                    + " ADD COLUMN " + SectionItemAnalyticsData.DATE + " INTEGER");
        }
    }

    private void upgradeTo21(SQLiteDatabase db) {
        try {
            createMostSearchesTriggers(db);
        } catch (SQLiteException ex) {
            Crashlytics.logException(ex);
        }
        try {
            db.execSQL("DROP TABLE IF EXISTS " + CategoryAdapter.TABLE_NAME);
        } catch (SQLiteException ex) {
            Crashlytics.logException(ex);
        }
        try {
            db.execSQL("DROP TABLE IF EXISTS " + AreaPinInfoDbHelper.TABLE_NAME);
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    private void initializeDatabase(SQLiteDatabase db) {
        db.execSQL(SubCategoryAdapter.CREATE_TABLE);
        db.execSQL(SearchSuggestionDbHelper.CREATE_TABLE);
        db.execSQL(MostSearchesDbHelper.CREATE_TABLE);
        db.execSQL(DynamicPageDbHelper.CREATE_TABLE);  // Added in version 16
        db.execSQL(AppDataDynamicDbHelper.CREATE_TABLE);  // Added in version 16
        createSectionItemAnalyticsTable(db);
        createMostSearchesTriggers(db);
    }

    private void createSectionItemAnalyticsTable(SQLiteDatabase db) {
        String createStmt = "CREATE TABLE IF NOT EXISTS " + SectionItemAnalyticsData.TABLE_NAME
                + " ( " + SectionItemAnalyticsData.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SectionItemAnalyticsData.SECTION_ID + " TEXT, "
                + SectionItemAnalyticsData.CITY_ID + " TEXT, "
                + SectionItemAnalyticsData.CLICKS + " INTEGER, "
                + SectionItemAnalyticsData.IMPRESSIONS + " INTEGER, "
                + SectionItemAnalyticsData.ANALYTICS_ATTRS + " TEXT, "
                + SectionItemAnalyticsData.DATE + " INTEGER );";
        db.execSQL(createStmt);
    }

    private void createMostSearchesTriggers(SQLiteDatabase db) {
        String tableName = MostSearchesDbHelper.TABLE_NAME;
        String id = MostSearchesDbHelper.ID;
        String searchCount = MostSearchesDbHelper.COUNT;
        String mostSearchesUpdateTriggerStatement =
                "CREATE TRIGGER IF NOT EXISTS most_searches_update_usage_trigger"
                + " AFTER UPDATE ON " + tableName
                + " BEGIN "
                + " UPDATE OR IGNORE " + tableName + " SET "
                + searchCount + " = " + searchCount + " + 1 WHERE "
                + MostSearchesDbHelper.QUERY + " = OLD." + MostSearchesDbHelper.QUERY + " ; "
                + " END ";
        db.execSQL(mostSearchesUpdateTriggerStatement);


        String mostSearchesDeleteLeastUsedTriggerStatement =
            "CREATE TRIGGER IF NOT EXISTS most_searches_delete_least_searched_item_trigger"
            + " AFTER INSERT ON " + tableName
            + " BEGIN "
            + " DELETE FROM " + tableName + " WHERE "
                + id + " = ( SELECT MIN(" + id + ") FROM " + tableName
                    + " WHERE 20 < (SELECT COUNT(" + id + ") FROM " + tableName + ") AND "
                    + searchCount + " = (SELECT MIN(" + searchCount + ") FROM " + tableName + ") );"
            + " END ";
        db.execSQL(mostSearchesDeleteLeastUsedTriggerStatement);
    }

    private static boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", (String[])null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");
                if(nameIndex < 0) {
                    return false;
                }
                do {
                    String name = cursor.getString(nameIndex);
                    if(name.equals(columnName)) {
                        return  true;
                    }
                } while(cursor.moveToNext());
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

}
