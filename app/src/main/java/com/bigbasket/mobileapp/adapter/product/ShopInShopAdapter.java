package com.bigbasket.mobileapp.adapter.product;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.model.general.ShopInShop;

import java.util.ArrayList;

public class ShopInShopAdapter {

    private Context context;

    public ShopInShopAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SLUG = "slug";
    public static final String COLUMN_IS_EXPRESS = "is_express";
    public static final String COLUMN_IS_DISCOUNT = "is_discount";
    public static final String tableName = "shopinshop";

    public static final String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "%3$s TEXT NOT NULL, %4$s TEXT NOT NULL, %5$s INTEGER DEFAULT 0, %6$s INTEGER DEFAULT 0);",
            tableName, COLUMN_ID, COLUMN_NAME, COLUMN_SLUG, COLUMN_IS_EXPRESS, COLUMN_IS_DISCOUNT);

    public void insertAll(ArrayList<ShopInShop> shopInShops) {
        for (ShopInShop shopInShop: shopInShops) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME, shopInShop.getName());
            contentValues.put(COLUMN_SLUG, shopInShop.getSlug());
            contentValues.put(COLUMN_IS_EXPRESS, shopInShop.getIsExpress());
            contentValues.put(COLUMN_IS_DISCOUNT, shopInShop.getIsDiscount());
            DatabaseHelper.db.insert(tableName, null, contentValues);
        }
    }

    public ArrayList<ShopInShop> getAllShopInShops() {
        ArrayList<ShopInShop> shopInShops = null;
        Cursor cursor = null;
        try {
            cursor = getCursorForAllRows();
            if (cursor.moveToFirst()) {
                shopInShops = new ArrayList<>();
                do {
                    shopInShops.add(new ShopInShop(cursor));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return shopInShops;
    }

    public void deleteAll() {
        DatabaseHelper.db.delete(tableName, null, null);
    }

    public static Cursor getCursorForAllRows() {
        return DatabaseHelper.db.query(tableName, new String[]{COLUMN_NAME, COLUMN_SLUG, COLUMN_IS_DISCOUNT, COLUMN_IS_EXPRESS}
                , null, null, null, null, null);
    }

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }
}
