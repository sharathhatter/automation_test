package com.bigbasket.mobileapp.devconfig;

import android.content.Context;
import android.content.SharedPreferences;

import com.bigbasket.mobileapp.R;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

/**
 * Created by bigbasket on 4/11/15.
 */
public class DeveloperConfigs {
    public static final String DEVELOPER_PREF_FILE = "developer_configs";

    public static final String IS_DEVELOPER = "is_developer";
    public static final String MAPI_SERVER_ADDRESS = "server_address";
    public static final String HTTP_LOGGING_LEVEL = "http_logging_level";

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

    public static void saveHttpLoggingLevel(Context context, String newValue) {
        getDevPrefrences(context.getApplicationContext())
                .edit().putString(HTTP_LOGGING_LEVEL, newValue).apply();
    }

    public static HttpLoggingInterceptor.Level getHttpLoggingLevel(Context context) {
        try {
            String loggingLevel = getDevPrefrences(context.getApplicationContext())
                    .getString(HTTP_LOGGING_LEVEL,
                            String.valueOf(HttpLoggingInterceptor.Level.NONE));
            return HttpLoggingInterceptor.Level.valueOf(loggingLevel);
        } catch (Throwable t){
            //Ignore
        }
        return HttpLoggingInterceptor.Level.NONE;
    }

    public static Interceptor getHttpLoggingInterceptor(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(getHttpLoggingLevel(context));
        return interceptor;
    }
}
