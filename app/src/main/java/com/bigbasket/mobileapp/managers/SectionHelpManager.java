package com.bigbasket.mobileapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

public final class SectionHelpManager {
    private static final String HELP_KEY_SET = "help_key_set";
    // Using different keys since user can always upgrade the OS

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
        return preferences.getStringSet(HELP_KEY_SET, null);
    }

    private static void storeHelpKeyName(SharedPreferences preferences, @NonNull Set<String> readHelpSet) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(HELP_KEY_SET, readHelpSet);
        editor.apply();
    }
}
