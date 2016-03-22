package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider;

import java.util.ArrayList;


public class AreaPinInfoDbHelper {

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_PIN = "pincode";
    public static final String COLUMN_AREA = "area";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_CITY_ID = "city_id";
    public static final String TABLE_NAME = "areaPinInfo";
    public static final String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT , %4$s TEXT , %5$s TEXT , " +
                    "%6$s INTEGER);", TABLE_NAME, COLUMN_ID, COLUMN_PIN, COLUMN_AREA,
            COLUMN_CITY, COLUMN_CITY_ID);

    /**
     * Content URI for the content provider to manipulate this table(areaPinInfo)
     */
    public static final Uri CONTENT_URI =
            Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);


    private Context context;

    public AreaPinInfoDbHelper(Context context) {
        this.context = context;
    }

    public String getAreaPin(String areaName, String cityName) {

        Cursor areaPinCursor = null;
        String pinCode = null;

        /**
         * name of the columns required.
         */
        String[] projection = {COLUMN_PIN};
        try {
            areaPinCursor = context.getContentResolver().query(CONTENT_URI, projection, COLUMN_AREA + " = " + "\"" + areaName + "\" AND " +
                    COLUMN_CITY + " = \"" + cityName + "\"", null, null);
            if (areaPinCursor != null && areaPinCursor.moveToFirst()) {
                pinCode = areaPinCursor.getString(areaPinCursor.getColumnIndex(AreaPinInfoDbHelper.COLUMN_PIN));
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

        /**
         * name of the columns required.
         */
        String[] projection = {COLUMN_AREA};

        try {
            areaNameCursor = context.getContentResolver().query(CONTENT_URI, projection, COLUMN_CITY + " = \"" + cityName + "\"", null, COLUMN_AREA + " ASC");
            if (areaNameCursor != null && areaNameCursor.moveToFirst()) {
                do {
                    areaNameStr = areaNameCursor.getString(areaNameCursor.getColumnIndex(AreaPinInfoDbHelper.COLUMN_AREA));
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
        /**
         * name of the columns required.
         * using Distinct to get the unique values from database
         */
        String[] projection = {"Distinct " + COLUMN_PIN};

        try {
            areaPinCursor = context.getContentResolver().query(CONTENT_URI, projection, COLUMN_CITY + " = \"" + cityName + "\"", null, COLUMN_PIN + " ASC");
            if (areaPinCursor != null && areaPinCursor.moveToFirst()) {
                do {
                    areaNameStr = areaPinCursor.getString(areaPinCursor.getColumnIndex(AreaPinInfoDbHelper.COLUMN_PIN));
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

        /**
         * name of the columns required.
         */
        String[] projection = {COLUMN_AREA};
        try {
            areaNameCursor = context.getContentResolver().query(CONTENT_URI, projection, COLUMN_PIN + " = " + "\"" + pinCode + "\" AND " +
                    COLUMN_CITY + " = \"" + cityName + "\"", null, null);
            if (areaNameCursor != null && areaNameCursor.moveToFirst()) {
                do {
                    String areaNameStr = areaNameCursor.getString(areaNameCursor.getColumnIndex(AreaPinInfoDbHelper.COLUMN_AREA));
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

    public static void clearAll(Context context) {
        context.getContentResolver().delete(CONTENT_URI, null, null);
    }
}
