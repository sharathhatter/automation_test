package com.bigbasket.mobileapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.account.AddressSummary;
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

public final class AddressManager {
    private AddressManager() {
    }

    private static final String PREFERENCE_KEY = "stored_addresses";
    private static final String PREFERENCE_TIMEOUT_KEY = "stored_addresses_timeout";
    private static final long TIMEOUT = 2;  // minutes

    @Nullable
    public static ArrayList<AddressSummary> getStoredAddresses(@Nullable Context context) {
        if (context == null) return null;
        if (isStale(context)) return null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String addressJson = preferences.getString(PREFERENCE_KEY, null);
        if (!TextUtils.isEmpty(addressJson)) {
            Type collectionType = new TypeToken<Collection<AddressSummary>>() {
            }.getType();
            return new Gson().fromJson(addressJson, collectionType);
        }
        return null;
    }

    public static boolean isStale(@Nullable Context context) {
        if (context == null) return true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastFetchedStr = preferences.getString(PREFERENCE_TIMEOUT_KEY, null);
        if (TextUtils.isEmpty(lastFetchedStr)) return true;
        try {
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            Date lastCalledDate = format.parse(lastFetchedStr);
            Date now = format.getCalendar().getTime();
            long diff = now.getTime() - lastCalledDate.getTime();
            return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS) > TIMEOUT;
        } catch (ParseException e) {
            return true;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static void storeAddresses(@Nullable Context context,
                                      @NonNull ArrayList<AddressSummary> addressSummaries) {
        if (context == null) return;
        String addressJson = new Gson().toJson(addressSummaries);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY, addressJson);
        editor.putString(PREFERENCE_TIMEOUT_KEY, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault()).format(new Date()));
        editor.apply();
    }

    public static void reset(@Nullable Context context) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(PREFERENCE_TIMEOUT_KEY)
                .remove(PREFERENCE_KEY)
                .apply();
    }
}
