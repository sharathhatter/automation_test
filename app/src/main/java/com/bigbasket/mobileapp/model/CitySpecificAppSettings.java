package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.util.Constants;

public class CitySpecificAppSettings {
    public static CitySpecificAppSettings citySpecificAppSettings;

    private boolean hasBundlePack;

    private CitySpecificAppSettings(Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefer != null) {
            hasBundlePack = prefer.getBoolean(Constants.HAS_BUNDLE_PACK, false);
        }
    }

    public static void clearInstance(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(Constants.HAS_BUNDLE_PACK);
        editor.commit();
        citySpecificAppSettings = null;
    }

    public static void setHasBundlePack(boolean hasBundlePack, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(Constants.HAS_BUNDLE_PACK, hasBundlePack);
        editor.commit();
        if (citySpecificAppSettings != null) {
            citySpecificAppSettings.hasBundlePack = hasBundlePack;
        } else {
            citySpecificAppSettings = new CitySpecificAppSettings(context);
        }
    }

    public static CitySpecificAppSettings getInstance(Context context) {
        if (citySpecificAppSettings == null) {
            citySpecificAppSettings = new CitySpecificAppSettings(context);
        }
        return citySpecificAppSettings;
    }

    public boolean hasBundlePack() {
        return hasBundlePack;
    }
}
