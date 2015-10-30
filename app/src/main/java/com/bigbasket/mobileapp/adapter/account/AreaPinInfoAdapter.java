package com.bigbasket.mobileapp.adapter.account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider;
import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;

import java.util.ArrayList;


public class AreaPinInfoAdapter {

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_PIN = "pincode";
    public static final String COLUMN_AREA = "area";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_CITY_ID = "city_id";
    public static final String tableName = "areaPinInfo";
    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT , %4$s TEXT , %5$s TEXT , " +
                    "%6$s INTEGER);", tableName, COLUMN_ID, COLUMN_PIN, COLUMN_AREA,
            COLUMN_CITY, COLUMN_CITY_ID);

    /**
     * Content URI for the content provider to manipulate this table(areaPinInfo)
     */
    public static final Uri CONTENT_URI = Uri.parse(DatabaseContentProvider.CONTENT_URI_PREFIX
            + "/" + tableName);


    private Context context;

    public AreaPinInfoAdapter(Context context) {
        this.context = context;
        open();
    }

    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void insert(String areaName, String pinCode, String cityName, int cityId) {
        try {
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_PIN, pinCode);
            cv.put(COLUMN_AREA, areaName);
            cv.put(COLUMN_CITY, cityName);
            cv.put(COLUMN_CITY_ID, cityId);
//            DatabaseHelper.db.insert(tableName, null, cv);
            /**
             * inserting values uing content provider
             */
            context.getContentResolver().insert(CONTENT_URI,cv);

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public String getAreaPin(String areaName, String cityName) {

        Cursor areaPinCursor = null;
        String pinCode = null;
        try {
            areaPinCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_PIN}
                    , COLUMN_AREA + " = " + "\"" + areaName + "\" AND " +
                    COLUMN_CITY + " = \"" + cityName + "\"", null, null, null, null);
            if (areaPinCursor != null && areaPinCursor.moveToFirst()) {
                pinCode = areaPinCursor.getString(areaPinCursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_PIN));
            }
        } catch (SQLiteException ex) {
            ex.getStackTrace();
        } finally {
            if (areaPinCursor != null && !areaPinCursor.isClosed()) {
                areaPinCursor.close();
            }
        }
        return pinCode;
    }

    @NonNull
    public ArrayList<String> getAreaNameList(String cityName) {
        Cursor areaNameCursor = null;
        String areaNameStr;
        ArrayList<String> result = new ArrayList<>();
        try {
            areaNameCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_AREA}
                    , COLUMN_CITY + " = \"" + cityName + "\"", null, null, null, COLUMN_AREA + " ASC");
            if (areaNameCursor != null && areaNameCursor.moveToFirst()) {
                do {
                    areaNameStr = areaNameCursor.getString(areaNameCursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_AREA));
                    result.add(areaNameStr);
                } while (areaNameCursor.moveToNext());
            }
        } catch (SQLiteException ex) {
            ex.getStackTrace();
        } finally {
            if (areaNameCursor != null && !areaNameCursor.isClosed()) {
                areaNameCursor.close();
            }
        }
        return result;
    }

    @NonNull
    public ArrayList<String> getPinList(String cityName) {
        Cursor areaPinCursor = null;
        String areaNameStr;
        ArrayList<String> result = new ArrayList<>();
        try {
            areaPinCursor = DatabaseHelper.db.query(true, tableName, new String[]{COLUMN_PIN}
                    , COLUMN_CITY + " = \"" + cityName + "\"", null, null, null, COLUMN_PIN + " ASC", null);
            if (areaPinCursor != null && areaPinCursor.moveToFirst()) {
                do {
                    areaNameStr = areaPinCursor.getString(areaPinCursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_PIN));
                    result.add(areaNameStr);
                } while (areaPinCursor.moveToNext());
            }
        } catch (SQLiteException ex) {
            ex.getStackTrace();
        } finally {
            if (areaPinCursor != null && !areaPinCursor.isClosed()) {
                areaPinCursor.close();
            }
        }
        return result;
    }

    @NonNull
    public ArrayList<String> getAreaName(String pinCode, String cityName) {
        Cursor areaNameCursor = null;
        ArrayList<String> areaNameArrayList = new ArrayList<>();
        try {
            areaNameCursor = DatabaseHelper.db.query(tableName, new String[]{COLUMN_AREA}
                    , COLUMN_PIN + " = " + "\"" + pinCode + "\" AND " +
                    COLUMN_CITY + " = \"" + cityName + "\"", null, null, null, null);
            if (areaNameCursor != null && areaNameCursor.moveToFirst()) {
                do {
                    String areaNameStr = areaNameCursor.getString(areaNameCursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_AREA));
                    areaNameArrayList.add(areaNameStr);
                } while (areaNameCursor.moveToNext());
            }
        } catch (SQLiteException ex) {
            ex.getStackTrace();
        } finally {
            if (areaNameCursor != null && !areaNameCursor.isClosed()) {
                areaNameCursor.close();
            }
        }
        return areaNameArrayList;
    }

    public void deleteData() {
        DatabaseHelper.db.execSQL("DELETE FROM " + tableName);
    }
}
