package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CityManager {

    public static boolean isAreaPinInfoDataValidStale(Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        String areaInfoCalledLast = prefer.getString(Constants.AREA_INFO_CALL_LAST, null);
        try {
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date d1 = format.getCalendar().getTime();
            int days = 0;
            if (areaInfoCalledLast != null) {
                Date d2 = format.parse(areaInfoCalledLast);
                long diff = d1.getTime() - d2.getTime();
                days = (int) diff / (24 * 60 * 60 * 1000);
            }
            return areaInfoCalledLast == null || days > 30;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void setAreaPinInfoDate(Context context) {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date d1 = format.getCalendar().getTime();
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefer.edit();
        String currentDate = format.format(d1);
        editor.putString(Constants.AREA_INFO_CALL_LAST, currentDate);
        editor.apply();
    }

    public static boolean hasUserChosenCity(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(Constants.HAS_USER_CHOSEN_CITY, false);
    }
}
