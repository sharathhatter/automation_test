package com.bigbasket.mobileapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class SectionHelpManager {
    private static final String HELP_KEY_SET = "help_key_set";
    // Using different keys since user can always upgrade the OS
    private static final String HELP_KEY_SET_GINGERBREAD = "help_key_set_ginger";

    private SectionHelpManager() {
    }

    public static void markAsRead(Context context, String helpKey) {
        if (context == null || TextUtils.isEmpty(helpKey)) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> alreadyReadHelp = getStoredHelpKeySet(preferences);
        if (alreadyReadHelp == null) {
            alreadyReadHelp = new HashSet<>();
        }
        alreadyReadHelp.add(helpKey);
        storeHelpKeyName(preferences, alreadyReadHelp);
    }

    public static boolean isRead(Context context, String helpKey) {
        if (context == null || TextUtils.isEmpty(helpKey)) return true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> alreadyReadHelp = getStoredHelpKeySet(preferences);
        return alreadyReadHelp != null && alreadyReadHelp.contains(helpKey);
    }

    @Nullable
    private static Set<String> getStoredHelpKeySet(SharedPreferences preferences) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(HELP_KEY_SET, null);
        } else {
            String helpSetJson = preferences.getString(HELP_KEY_SET_GINGERBREAD, null);
            if (TextUtils.isEmpty(helpSetJson)) {
                return null;
            }
            Type type = new TypeToken<Set<String>>() {
            }.getType();
            return new Gson().fromJson(helpSetJson, type);
        }
    }

    private static void storeHelpKeyName(SharedPreferences preferences, @NonNull Set<String> readHelpSet) {
        SharedPreferences.Editor editor = preferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(HELP_KEY_SET, readHelpSet);
        } else {
            String helpSetJson = new Gson().toJson(readHelpSet);
            editor.putString(HELP_KEY_SET_GINGERBREAD, helpSetJson);
        }
        editor.apply();
    }
}
