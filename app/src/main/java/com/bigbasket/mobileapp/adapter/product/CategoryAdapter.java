package com.bigbasket.mobileapp.adapter.product;


import android.content.ContentValues;
import android.content.Context;

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
}
