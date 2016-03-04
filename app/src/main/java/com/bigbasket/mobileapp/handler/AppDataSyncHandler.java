package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.newrelic.agent.android.instrumentation.Trace;

public class AppDataSyncHandler {

    public static final String LAST_APP_DATA_CALL_TIME = "app_data_call_time";
    private static final String LAST_APP_DATA_CALL_CITY_NAME = "app_data_last_call_city";

    @Trace
    public static boolean isSyncNeeded(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastAppDataCallTime = preferences.getLong(LAST_APP_DATA_CALL_TIME, 0);
        String currentCity = getCurrentCity(context);
        String lastSyncedForCity = preferences.getString(LAST_APP_DATA_CALL_CITY_NAME, null);
        return lastAppDataCallTime == 0 || UIUtil.isMoreThanXHour(lastAppDataCallTime, Constants.SIX_HOUR)
                || (TextUtils.isEmpty(currentCity) || TextUtils.isEmpty(lastSyncedForCity)
                || !currentCity.equalsIgnoreCase(lastSyncedForCity));
    }

    @Trace
    public static void updateLastAppDataCall(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LAST_APP_DATA_CALL_TIME, System.currentTimeMillis());
        editor.putString(LAST_APP_DATA_CALL_CITY_NAME, preferences.getString(Constants.CITY, ""));
        editor.apply();
    }

    @Trace
    public static void reset(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LAST_APP_DATA_CALL_TIME);
        editor.remove(LAST_APP_DATA_CALL_CITY_NAME);
        editor.apply();
    }

    @Trace
    @Nullable
    private static String getCurrentCity(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.CITY, null);
    }
}
