package com.bigbasket.mobileapp.adapter.product;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.ResponseSerializer;

import java.util.ArrayList;

public class SubCategoryAdapter {

    private Context context;

    public SubCategoryAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_BLOB = "subCategoryResponse";
    public static final String COLUMN_SECTION_DATA = "sectionData";
    public static final String COLUMN_SLUG = "slug";
    public static final String tableName = "subcategory";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT , %4$s TEXT ,%5$s BLOB, %6$s BLOB NULL);", tableName, COLUMN_ID, COLUMN_VERSION,
            COLUMN_SLUG, COLUMN_BLOB, COLUMN_SECTION_DATA);

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }

    public void insert(SubCategoryModel subCategoryModel, String version, SectionData sectionData, String slug) { //, ArrayList<String> bannerArrayList,
        Log.d("Inserting sub_categories to database", "");
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_VERSION, version);
        cv.put(COLUMN_SLUG, slug);
        byte[] bytesCategories = ResponseSerializer.serializeObject(subCategoryModel);
        cv.put(COLUMN_BLOB, bytesCategories);
        if (sectionData != null) {
            byte[] bytesSection = ResponseSerializer.serializeObject(sectionData);
            cv.put(COLUMN_SECTION_DATA, bytesSection);
        }
        DatabaseHelper.db.insert(tableName, null, cv);

    }

    public static Cursor getCursorForAllRows() {
        return DatabaseHelper.db.query(tableName, new String[]{COLUMN_VERSION, COLUMN_BLOB}
                , null, null, null, null, null);
    }

    public ArrayList<Object> getSubCategory(String slug) {
        Log.d("Inserting getAllSubCategories Method ", "");
        Cursor subCategoryCursor = null;
        ArrayList<Object> result = new ArrayList<>();
        try {
            subCategoryCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_BLOB, COLUMN_SECTION_DATA}
                    , COLUMN_SLUG + " = " + "\"" + slug + "\"", null, null, null, null);
            if (subCategoryCursor.moveToFirst()) {
                byte[] subCategoryCursorBlob = subCategoryCursor.getBlob(
                        subCategoryCursor.getColumnIndex(SubCategoryAdapter.COLUMN_BLOB));
                result.add(ResponseSerializer.deserializeObject(subCategoryCursorBlob));
                byte[] sectionDataCursorBlob = subCategoryCursor.getBlob(
                        subCategoryCursor.getColumnIndex(SubCategoryAdapter.COLUMN_SECTION_DATA));
                result.add(ResponseSerializer.deserializeObject(sectionDataCursorBlob));
            }
        } catch (SQLiteException ex) {

        } finally {
            if (subCategoryCursor != null && !subCategoryCursor.isClosed()) {
                subCategoryCursor.close();
            }
        }
        return result;
    }

    public String getVersion(String slug) {
        Log.d("Inside getVersion Method ", "");
        Cursor cursor = null;
        String version = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_VERSION, COLUMN_BLOB}
                    , COLUMN_SLUG + " = " + "\"" + slug + "\"", null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                byte[] blob  = cursor.getBlob(cursor.getColumnIndex(COLUMN_BLOB));
                if (blob != null && blob.length >= 0) {
                    version = cursor.getString(cursor.getColumnIndex(COLUMN_VERSION));
                }
            }
        } catch (SQLiteException ex) {
            ex.getStackTrace();

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return version;

    }


}

