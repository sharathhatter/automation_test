package com.bigbasket.mobileapp.adapter.product;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.ResponseSerializer;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

public class SubCategoryAdapter {

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_BLOB = "subCategoryResponse";
    public static final String COLUMN_SECTION_DATA = "sectionData";
    public static final String COLUMN_SLUG = "slug";
    public static final String TABLE_NAME = "subcategory";
    public static final String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT , %4$s TEXT ,%5$s BLOB, %6$s BLOB NULL);", TABLE_NAME, COLUMN_ID, COLUMN_VERSION,
            COLUMN_SLUG, COLUMN_BLOB, COLUMN_SECTION_DATA);


    public static final Uri CONTENT_URI = Uri.withAppendedPath(
            DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);
    public static final String MIME_TYPE_DIR =
            "vnd.android.cursor.dir/com.bigbasket.mobileapp.subcategory";
    public static final String MIME_TYPE_ITEM =
            "vnd.android.cursor.item/com.bigbasket.mobileapp.subcategory";

    public static void insert(Context context, SubCategoryModel subCategoryModel, String version,
                              SectionData sectionData, String slug) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_VERSION, version);
        cv.put(COLUMN_SLUG, slug);
        byte[] bytesCategories = ResponseSerializer.serializeObject(subCategoryModel);
        cv.put(COLUMN_BLOB, bytesCategories);
        if (sectionData != null) {
            byte[] bytesSection = ResponseSerializer.serializeObject(sectionData);
            cv.put(COLUMN_SECTION_DATA, bytesSection);
        }
        context.getContentResolver().insert(CONTENT_URI, cv);

    }

    public static ArrayList<Object> getSubCategory(Context context, String slug) {
        Cursor subCategoryCursor = null;
        ArrayList<Object> result = new ArrayList<>();
        try {
            subCategoryCursor = context.getContentResolver().query(CONTENT_URI,
                    new String[]{COLUMN_BLOB, COLUMN_SECTION_DATA},
                    COLUMN_SLUG + " = ?", new String[]{slug}, null);
            if (subCategoryCursor != null && subCategoryCursor.moveToFirst()) {
                byte[] subCategoryCursorBlob = subCategoryCursor.getBlob(0);
                result.add(ResponseSerializer.deserializeObject(subCategoryCursorBlob));
                byte[] sectionDataCursorBlob = subCategoryCursor.getBlob(1);
                result.add(sectionDataCursorBlob != null && sectionDataCursorBlob.length > 0 ?
                        ResponseSerializer.deserializeObject(sectionDataCursorBlob) : null);
            }
        } catch (SQLiteException ex) {
            Crashlytics.logException(ex);
        } finally {
            if (subCategoryCursor != null && !subCategoryCursor.isClosed()) {
                subCategoryCursor.close();
            }
        }
        return result;
    }

    public static String getVersion(Context context, String slug) {
        Cursor cursor = null;
        String version = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI,
                    new String[]{COLUMN_BLOB, COLUMN_VERSION},
                    COLUMN_SLUG + " = ?", new String[]{slug }, null);
            if (cursor != null && cursor.moveToFirst()) {
                byte[] blob = cursor.getBlob(0);
                if (blob != null && blob.length >= 0) {
                    version = cursor.getString(1);
                }
            }
        } catch (SQLiteException ex) {
            Crashlytics.logException(ex);

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return version;

    }


}

