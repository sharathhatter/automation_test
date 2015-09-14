package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class AppDataDynamic {
    private static final String TIMEOUT_KEY = "dynamic_data_timeout";
    private static final long TIMEOUT = 2;  // minutes

    private static volatile AppDataDynamic appDataDynamic;
    private static final Object lock = new Object();

    private ArrayList<AddressSummary> addressSummaries;
    private boolean isContextualMode;
    private String expressAvailability;

    public static AppDataDynamic getInstance(Context context) {
        AppDataDynamic localInstance = appDataDynamic;
        if (localInstance == null) {
            synchronized (lock) {
                localInstance = appDataDynamic;
                if (localInstance == null) {
                    localInstance = new AppDataDynamic(context);
                    appDataDynamic = localInstance;
                }
            }
        }
        return localInstance;
    }

    public static void updateInstance(@Nullable Context context,
                                      ArrayList<AddressSummary> addressSummaries,
                                      boolean isContextualMode,
                                      String expressAvailability) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (addressSummaries != null) {
            String addressJson = new Gson().toJson(addressSummaries);
            editor.putString(Constants.ADDRESSES, addressJson);
        }
        editor.putBoolean(Constants.IS_CONTEXTUAL_MODE, isContextualMode);
        editor.putString(TIMEOUT_KEY, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault()).format(new Date()));
        editor.putString(Constants.EXPRESS_AVAILABILITY, expressAvailability);
        editor.apply();
        appDataDynamic = null;
    }

    public static boolean isStale(@Nullable Context context) {
        if (context == null) return true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastFetchedStr = preferences.getString(TIMEOUT_KEY, null);
        if (TextUtils.isEmpty(lastFetchedStr)) return true;
        try {
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            Date lastCalledDate = format.parse(lastFetchedStr);
            Date now = new Date();
            long diff = now.getTime() - lastCalledDate.getTime();
            return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS) > TIMEOUT;
        } catch (ParseException e) {
            return true;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private AppDataDynamic(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String addressJson = preferences.getString(Constants.ADDRESSES, null);
        if (!TextUtils.isEmpty(addressJson)) {
            Type collectionType = new TypeToken<Collection<AddressSummary>>() {
            }.getType();
            addressSummaries = new Gson().fromJson(addressJson, collectionType);
        }

        isContextualMode = preferences.getBoolean(Constants.IS_CONTEXTUAL_MODE, false);
        expressAvailability = preferences.getString(Constants.EXPRESS_AVAILABILITY, null);
    }

    public static void reset(Context context) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(Constants.ADDRESSES)
                .remove(Constants.IS_CONTEXTUAL_MODE)
                .remove(TIMEOUT_KEY)
                .remove(Constants.EXPRESS_AVAILABILITY)
                .apply();
        appDataDynamic = null;
    }

    public ArrayList<AddressSummary> getAddressSummaries() {
        return addressSummaries;
    }

    public boolean isContextualMode() {
        return isContextualMode;
    }

    @Nullable
    public String getExpressAvailability() {
        return expressAvailability;
    }
}
