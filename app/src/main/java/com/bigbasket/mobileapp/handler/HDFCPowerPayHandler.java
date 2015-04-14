package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HDFCPowerPayHandler {
    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final int TIMEOUT_IN_MINUTES = 6 * 60;

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
        return !isStale(timeStr, dateFormat);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove("HDFC_PAY_START");
        editor.commit();
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
}
