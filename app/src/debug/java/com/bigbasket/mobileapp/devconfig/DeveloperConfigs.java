package com.bigbasket.mobileapp.devconfig;

import android.content.Context;
import android.content.SharedPreferences;

import com.bigbasket.mobileapp.R;

/**
 * Created by bigbasket on 4/11/15.
 */
public class DeveloperConfigs {
    public static final String DEVELOPER_PREF_FILE = "developer_configs";

    public static final String IS_DEVELOPER = "is_developer";
    public static final String MAPI_SERVER_ADDRESS = "server_address";

    private static SharedPreferences getDevPrefrences(Context context) {
        return context.getSharedPreferences(DEVELOPER_PREF_FILE, Context.MODE_PRIVATE);
    }


    public static void saveMapiServerAddress(Context context, String newServer){
        getDevPrefrences(context.getApplicationContext())
                .edit().putString(MAPI_SERVER_ADDRESS, newServer).apply();
    }

    public static String getMapiServerAddress(Context context){
        return getDevPrefrences(context.getApplicationContext()).getString(MAPI_SERVER_ADDRESS,
                context.getString(R.string.pref_default_server_address));
    }

    public static boolean isDeveloper(Context context){
        return getDevPrefrences(context.getApplicationContext()).getBoolean(IS_DEVELOPER, false);
    }

    public static void setIsDeveloper(Context context, boolean isDeveloper){
        getDevPrefrences(context.getApplicationContext())
                .edit().putBoolean(IS_DEVELOPER, isDeveloper).apply();
    }

}
