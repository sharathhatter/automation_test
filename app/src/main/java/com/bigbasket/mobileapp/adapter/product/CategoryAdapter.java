package com.bigbasket.mobileapp.adapter.product;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ResponseSerializer;

import java.util.ArrayList;

public class CategoryAdapter {

    private Context context;

    public CategoryAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_SLUG = "slug";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BLOB = "blob";
    public static final String COLUMN_IMAGE_PATH = "icon";
    public static final String COLUMN_FLAT_PAGE = "flat_page";
    public static final String tableName = "category";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT NOT NULL, %4$s TEXT NOT NULL, %5$s TEXT, %6$s TEXT, %7$s TEXT, %8$s BLOB );", tableName, COLUMN_ID, COLUMN_SLUG,
            COLUMN_VERSION, COLUMN_NAME, COLUMN_IMAGE_PATH, COLUMN_FLAT_PAGE, COLUMN_BLOB
    );

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }

    public void insert(ArrayList<TopCategoryModel> categoryListArray, String version) {
        Log.d("Inserting top_categories to database", "");
        DatabaseHelper.db.execSQL("DELETE FROM " + tableName);

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SLUG, Constants.TOP_MENU_SLUG);
        cv.put(COLUMN_VERSION, version);
        cv.put(COLUMN_NAME, Constants.TOP_MENU);
        cv.put(COLUMN_IMAGE_PATH, "");
        cv.put(COLUMN_FLAT_PAGE, Constants.FLAT_PAGE);
        byte[] bytesCategories = ResponseSerializer.serializeObject(categoryListArray);
        cv.put(COLUMN_BLOB, bytesCategories);
        DatabaseHelper.db.insert(tableName, null, cv);

        for (int i = 0; i < categoryListArray.size(); i++) {
            ContentValues cvc = new ContentValues();
            cvc.put(COLUMN_SLUG, categoryListArray.get(i).getSlug());
            cvc.put(COLUMN_VERSION, categoryListArray.get(i).getVersion());
            cvc.put(COLUMN_NAME, categoryListArray.get(i).getName());
            cvc.put(COLUMN_IMAGE_PATH, categoryListArray.get(i).getImagePath());
            cvc.put(COLUMN_FLAT_PAGE, categoryListArray.get(i).getFlatPage());
            cvc.put(COLUMN_BLOB, new byte[]{});
            DatabaseHelper.db.insert(tableName, null, cvc);
        }
    }

    public static Cursor getCursorForAllRows() {
        return DatabaseHelper.db.query(tableName, new String[]{COLUMN_VERSION, COLUMN_SLUG, COLUMN_BLOB}
                , null, null, null, null, null);
    }

    public ArrayList<TopCategoryModel> getAllTopCategories() {
        Log.d("Inserting getAllTopCategories Method ", "");
        Cursor categoryCursor = null;
        ArrayList<TopCategoryModel> topCategoryArrayList = null;
        try {
            categoryCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_SLUG, COLUMN_VERSION, COLUMN_NAME,
                    COLUMN_IMAGE_PATH, COLUMN_FLAT_PAGE, COLUMN_BLOB}
                    , COLUMN_SLUG + " != " + "\"" + Constants.TOP_MENU_SLUG + "\"", null, null, null, null);
            if (categoryCursor.moveToFirst()) {
                topCategoryArrayList = new ArrayList<>();
                do {
                    topCategoryArrayList.add(new TopCategoryModel(categoryCursor));
                } while (categoryCursor.moveToNext());
            }
        } catch (SQLiteException ex) {

        } finally {
            if (categoryCursor != null && !categoryCursor.isClosed()) {
                categoryCursor.close();
            }
        }
        return topCategoryArrayList;
    }

    public String getCategoriesVersion() {
        Log.d("Inside getCategoriesVersion Method ", "");
        Cursor cursor = null;
        String version = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_SLUG, COLUMN_VERSION, COLUMN_NAME,
                    COLUMN_FLAT_PAGE, COLUMN_BLOB}
                    , COLUMN_SLUG + " = " + "\"" + Constants.TOP_MENU_SLUG + "\"", null, null, null, null);
            if (cursor.moveToFirst()) {
                version = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_VERSION));

            }
        } catch (SQLiteException ex) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return version;

    }

    public String getImagePath(String catSlug) {
        Log.d("Inside getImagePath Method ", "");
        Cursor cursor = null;
        String imagePath = null;
        try {
            cursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_IMAGE_PATH}
                    , COLUMN_SLUG + " = " + "\"" + catSlug + "\"", null, null, null, null);
            if (cursor.moveToFirst()) {
                imagePath = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_VERSION));

            }
        } catch (SQLiteException ex) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return imagePath;
    }


}
