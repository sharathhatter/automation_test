package com.bigbasket.mobileapp.adapter.product;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.util.ResponseSerializer;

import java.lang.reflect.Array;
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
    public static final String COLUMN_BLOB_BANNER_URL = "banner";
    public static final String COLUMN_SLUG = "slug";
    public static final String tableName = "subcategory";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "%3$s TEXT , %4$s TEXT ,%5$s BLOB, %6$s BLOB );", tableName, COLUMN_ID, COLUMN_VERSION, COLUMN_SLUG, COLUMN_BLOB, COLUMN_BLOB_BANNER_URL);

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }

    public void insert(SubCategoryModel subCategoryModel, String version, ArrayList<String> bannerArrayList, String slug) {
        Log.d("Inserting sub_categories to database", "");
        try {
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_VERSION, version);
            cv.put(COLUMN_SLUG, slug);
            byte[] bytesCategories = ResponseSerializer.serializeObject(subCategoryModel);
            cv.put(COLUMN_BLOB, bytesCategories);
            byte[] bytesBannerUrl = ResponseSerializer.serializeObject(bannerArrayList);
            cv.put(COLUMN_BLOB_BANNER_URL, bytesBannerUrl);
            DatabaseHelper.db.insert(tableName, null, cv);
        } catch (Exception e) {
            e.getStackTrace();
        }
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
            subCategoryCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_BLOB, COLUMN_BLOB_BANNER_URL}
                    , COLUMN_SLUG + " = " + "\"" + slug + "\"", null, null, null, null);
            if (subCategoryCursor.moveToFirst()) {

                byte[] subCategoryCursorBlob = subCategoryCursor.getBlob(
                        subCategoryCursor.getColumnIndex(SubCategoryAdapter.COLUMN_BLOB));
                result.add(ResponseSerializer.deserializeObject(subCategoryCursorBlob));
                byte[] bannerUrlCursorBlob = subCategoryCursor.getBlob(
                        subCategoryCursor.getColumnIndex(SubCategoryAdapter.COLUMN_BLOB_BANNER_URL));
                result.add(ResponseSerializer.deserializeObject(bannerUrlCursorBlob));

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
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_VERSION}
                    , COLUMN_SLUG + " = " + "\"" + slug + "\"", null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                version = cursor.getString(cursor.getColumnIndex(SubCategoryAdapter.COLUMN_VERSION));
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

