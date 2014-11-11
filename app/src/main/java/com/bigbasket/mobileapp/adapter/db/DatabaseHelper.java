package com.bigbasket.mobileapp.adapter.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bigbasket.db";
    protected static final int DATABASE_VERSION = 7;
    public static SQLiteDatabase db = null;
    private static DatabaseHelper dbAdapter = null;
    private static boolean isConnectionOpen = false;

    public void open(Context context) {
        if (!isConnectionOpen) {
            try {
                Log.d("Opening connection to database in r/w mode for ", context.getClass().getSimpleName());
                db = getInstance(context).getWritableDatabase();
            } catch (SQLiteException e) {
                Log.d("Unable to get write access for database, so reverting to read-only mode", "");            //warn
                db = getInstance(context).getReadableDatabase();
            }
            isConnectionOpen = true;
        } else {
            Log.d("Database connection is already open", "");
        }
    }

    public void close() {
        if (isConnectionOpen) {
            Log.d("Database connection closed", "");
            db.close();
            isConnectionOpen = false;
        } else {
            Log.d("Database connection is already closed", "");
        }
    }

    public static DatabaseHelper getInstance(Context context) {
        if (dbAdapter == null)
            dbAdapter = new DatabaseHelper(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return dbAdapter;
    }

    private DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO : Jugal, plug-in data upgrade functionality
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        createTable(db);
    }


    private void createTable(SQLiteDatabase db) {
        db.execSQL(CategoryAdapter.createTable);
        db.execSQL(SubCategoryAdapter.createTable);
        db.execSQL(AreaPinInfoAdapter.createTable);
        db.execSQL(PrescriptionImageAdapter.createTable);
        db.execSQL(SearchSuggestionAdapter.createTable);
        db.execSQL(MostSearchesAdapter.createTable);
    }
}