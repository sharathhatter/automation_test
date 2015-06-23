package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HDFCPowerPayHandler {
    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final int DEFAULT_TIMEOUT_IN_MINUTES = 6 * 60;

    public static void setTimeOut(Context context, int timeOutInMinutes) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(Constants.HDFC_POWER_PAY_EXPIRY, timeOutInMinutes);
        editor.commit();
    }

    public static int getTimeoutInMinutes(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(Constants.HDFC_POWER_PAY_EXPIRY, DEFAULT_TIMEOUT_IN_MINUTES);
    }

    public static void setHDFCPayMode(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,
                Locale.getDefault());
        editor.putString("HDFC_PAY_START", dateFormat.format(new Date()));
        editor.commit();
    }

    public static boolean isInHDFCPayMode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timeStr = preferences.getString("HDFC_PAY_START", null);
        if (TextUtils.isEmpty(timeStr)) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,
                Locale.getDefault());
        return !isStale(context, timeStr, dateFormat);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove("HDFC_PAY_START");
        editor.commit();
    }

    private static boolean isStale(Context context,
                                   String createdOn, SimpleDateFormat simpleDateFormat) {
        try {
            Date createOnDate = simpleDateFormat.parse(createdOn);
            Date now = new Date();
            long minutes = TimeUnit.MINUTES.convert(now.getTime() - createOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return minutes > getTimeoutInMinutes(context);
        } catch (ParseException e) {
            return true;
        }
    }
}
