package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DataUtil {

    public static boolean isInternetAvailable(Context context) {
        return getConnectionStatus(context) == NetworkStatusCodes.NET_CONNECTED;
    }

    public static int getConnectionStatus(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (networkInfo.isConnected()) {
                return NetworkStatusCodes.NET_CONNECTED;
            } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                return NetworkStatusCodes.NET_CONNECTING;
            }
        }
        return NetworkStatusCodes.NET_DISCONNECTED;
    }

    public static String getAppVersion(Context context) {
        String appVersionName;
        try {
            appVersionName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = null;
        }
        return appVersionName;
    }
}