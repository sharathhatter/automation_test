package com.bigbasket.mobileapp.adapter.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.crashlytics.android.Crashlytics;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bigbasket.db";
    protected static final int DATABASE_VERSION = 16;
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
        if (newVersion >= 15) {
            try {
                db.execSQL("DELETE FROM " + CategoryAdapter.tableName);
                db.execSQL("DROP TABLE " + CategoryAdapter.tableName);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        try {
            db.execSQL("DELETE FROM " + SubCategoryAdapter.tableName);
            db.execSQL("DROP TABLE " + SubCategoryAdapter.tableName);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        try {
            db.execSQL("DELETE FROM " + AreaPinInfoAdapter.tableName);
            db.execSQL("DROP TABLE " + AreaPinInfoAdapter.tableName);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        if (oldVersion >= 16) {
            try {
                db.execSQL("DELETE FROM " + DynamicScreenAdapter.tableName);
                db.execSQL("DROP TABLE " + DynamicScreenAdapter.tableName);
                db.execSQL("DELETE FROM " + AppDataDynamicAdapter.tableName);
                db.execSQL("DROP TABLE " + AppDataDynamicAdapter.tableName);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(CategoryAdapter.createTable);
        db.execSQL(SubCategoryAdapter.createTable);
        db.execSQL(AreaPinInfoAdapter.createTable);
        db.execSQL(SearchSuggestionAdapter.createTable);
        db.execSQL(MostSearchesAdapter.createTable);
        db.execSQL(DynamicScreenAdapter.createTable);  // Added in version 16
        db.execSQL(AppDataDynamicAdapter.createTable);  // Added in version 16
    }
}