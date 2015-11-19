package com.bigbasket.mobileapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class CityManager {
    private CityManager() {
    }

    private static final int TIMEOUT_IN_MINUTES = 60;
    private static final String preferenceKey = "stored_city";

    public static boolean isAreaPinInfoDataStale(Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        String areaInfoCalledLast = prefer.getString(Constants.AREA_INFO_CALL_LAST, null);
        int cityCacheExpiryDays = prefer.getInt(preferenceKey + "_expiry", 7);
        try {
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date d1 = format.getCalendar().getTime();
            int days = 0;
            if (areaInfoCalledLast != null) {
                Date d2 = format.parse(areaInfoCalledLast);
                long diff = d1.getTime() - d2.getTime();
                days = (int) diff / (24 * 60 * 60 * 1000);
            }
            return areaInfoCalledLast == null || days > cityCacheExpiryDays;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static ArrayList<City> getStoredCity(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String storedCitiesJson = preferences.getString(preferenceKey, null);
        if (!TextUtils.isEmpty(storedCitiesJson)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault());
            String createdOn = preferences.getString(preferenceKey + "_time", null);
            if (!isStale(createdOn, dateFormat)) {
                Type collectionType = new TypeToken<Collection<City>>() {
                }.getType();
                return new Gson().fromJson(storedCitiesJson, collectionType);
            }
        }
        return null;
    }

    @Nullable
    public static City getCity(int cityId, Context context) {
        ArrayList<City> cities = getStoredCity(context);
        if (cities != null && cities.size() > 0) {
            for (City city : cities) {
                if (city.getId() == cityId) {
                    return city;
                }
            }
        }
        return null;
    }

    public static void storeCities(Context context, ArrayList<City> cities) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storedCityJson = new Gson().toJson(cities);
        editor.putString(preferenceKey, storedCityJson);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault());
        editor.putString(preferenceKey + "_time", dateFormat.format(new Date()));
        editor.apply();
    }

    public static void setCityCacheExpiry(Context context, int numDays) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putInt(preferenceKey + "_expiry", numDays);
        editor.apply();
    }

    private static boolean isStale(String createdOn, SimpleDateFormat simpleDateFormat) {
        try {
            Date createOnDate = simpleDateFormat.parse(createdOn);
            Date now = new Date();
            long minutes = TimeUnit.MINUTES.convert(now.getTime() - createOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return minutes > TIMEOUT_IN_MINUTES;
        } catch (ParseException e) {
            return true;
        }
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

    public static void clearChosenCity(Context context) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().remove(Constants.HAS_USER_CHOSEN_CITY).apply();
    }
}
