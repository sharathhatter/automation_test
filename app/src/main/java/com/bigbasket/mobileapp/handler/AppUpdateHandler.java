package com.bigbasket.mobileapp.handler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUpdateHandler {
    public static class AppUpdateData {
        private String appExpireBy;
        private String appUpdateMsg;
        private String latestAppVersion;

        public AppUpdateData(String appExpireBy, String appUpdateMsg, String latestAppVersion) {
            this.appExpireBy = appExpireBy;
            this.appUpdateMsg = appUpdateMsg;
            this.latestAppVersion = latestAppVersion;
        }

        @Nullable
        public String getAppExpireBy() {
            return appExpireBy;
        }

        public String getAppUpdateMsg() {
            return appUpdateMsg;
        }

        public String getLatestAppVersion() {
            return latestAppVersion;
        }
    }

    @Nullable
    public static AppUpdateData isOutOfDate(@Nullable Context context) {
        if (context == null) return null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String appExpireBy = preferences.getString(Constants.APP_EXPIRE_BY, null);
        if (TextUtils.isEmpty(appExpireBy)) return null;
        return new AppUpdateData(appExpireBy,
                preferences.getString(Constants.APP_UPDATE_MSG, ""),
                preferences.getString(Constants.LATEST_APP_VERSION, ""));
    }

    public static void markAsOutOfDate(@Nullable Context context, String appExpiredBy, String upgradeMsg, String latestAppVersion) {
        if (context == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.APP_EXPIRE_BY, appExpiredBy);
        editor.putString(Constants.APP_UPDATE_MSG, upgradeMsg);
        editor.putString(Constants.LATEST_APP_VERSION, latestAppVersion);
        editor.apply();
    }

    public static void markAsCurrent(@Nullable Context context) {
        if (context == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.APP_EXPIRE_BY);
        editor.remove(Constants.APP_UPDATE_MSG);
        editor.remove(Constants.LATEST_APP_VERSION);
        editor.apply();
    }

    public static int handleUpdateDialog(String serverAppExpireDateString, @Nullable Activity activity) {
        if (activity == null) return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
        long lastPopUpShownTime = prefer.getLong(Constants.LAST_POPUP_SHOWN_TIME, 0);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP, Locale.getDefault());

        String today = UIUtil.getToday(Constants.DATE_FORMAT_FOR_APP_UPGRADE_POPUP);

        Date serverAppExpireDate, toDaysData;
        try {
            serverAppExpireDate = sdf.parse(serverAppExpireDateString);
            toDaysData = sdf.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
            return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        }

        if (serverAppExpireDate.compareTo(toDaysData) < 0) {
            prefer.edit().putLong(AppDataSyncHandler.LAST_APP_DATA_CALL_TIME, 0).apply();
            return Constants.SHOW_APP_EXPIRE_POPUP;
        }
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        long timeDiff = (serverAppExpireDate.getTime() - lastPopUpShownTime) / (24 * 60 * 60 * 1000);

        if (timeDiff >= 0) {
            if (UIUtil.isMoreThanXDays(lastPopUpShownTime, Constants.ONE_DAY)) {
                if (popUpShownTimes < 3) {
                    return Constants.SHOW_APP_UPDATE_POPUP;
                } else {
                    if (UIUtil.isMoreThanXDays(lastPopUpShownTime, Constants.SIX_DAYS)) {
                        return Constants.SHOW_APP_UPDATE_POPUP;
                    } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
                }
            } else return Constants.DONT_SHOW_APP_UPDATE_POPUP;
        } else {
            prefer.edit().putLong(AppDataSyncHandler.LAST_APP_DATA_CALL_TIME, 0).apply();
            return Constants.SHOW_APP_EXPIRE_POPUP;
        }
    }

    public static void updateLastPopShownDate(long lastPopShownTime, @Nullable Activity activity) {
        if (activity == null) return;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putLong(Constants.LAST_POPUP_SHOWN_TIME, lastPopShownTime);
        int popUpShownTimes = prefer.getInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, 0);
        editor.putInt(Constants.APP_EXPIRE_POPUP_SHOWN_TIMES, popUpShownTimes + 1);
        editor.apply();
    }
}
