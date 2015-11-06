package com.bigbasket.mobileapp.devconfig;

import android.content.Context;

import com.bigbasket.mobileapp.R;

/**
 * Dummy class
 */
public class DeveloperConfigs {
    public static boolean isDeveloper(Context context) {
        return false;
    }

    public static String getMapiServerAddress(Context context) {
        return context.getString(R.string.pref_default_server_address);
    }
}
