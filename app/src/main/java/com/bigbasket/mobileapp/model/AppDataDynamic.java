package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.AppDataDynamicAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.AppDataDynamicResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class AppDataDynamic {
    private static final String TIMEOUT_KEY = "dynamic_data_timeout";
    private static final long TIMEOUT = 2;  // minutes

    private static volatile AppDataDynamic appDataDynamic;
    private static final Object lock = new Object();

    private ArrayList<String> addToBasketPostParams;
    private ArrayList<AddressSummary> addressSummaries;
    private boolean isContextualMode;
    private String expressAvailability;
    private String abModeName;  // Used for analytics
    private HashMap<String, String> storeAvailabilityMap;
    private HashMap<String, SpecialityStoresInfoModel> specialityStoreDetailList;

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

    public boolean updateInstance(Context context,
                                  Cursor cursor) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        boolean success = true;
        if (cursor != null && cursor.moveToFirst()) {
            String appDataDynamicJson = cursor.getString(cursor.getColumnIndex(AppDataDynamicAdapter.COLUMN_APP_DATA_DYNAMIC_PARAMS));
            if (!TextUtils.isEmpty(appDataDynamicJson)) {
                Gson gson = new Gson();
                AppDataDynamicResponse appDataDynamicResponse = gson.fromJson(appDataDynamicJson, AppDataDynamicResponse.class);
                if (appDataDynamicResponse.status == 0) {
                    this.addressSummaries = appDataDynamicResponse.getAppDataDynamicResponse.addressSummaries;
                    this.addToBasketPostParams = appDataDynamicResponse.getAppDataDynamicResponse.addToBasketPostParams;
                    this.isContextualMode = appDataDynamicResponse.getAppDataDynamicResponse.isContextualMode;
                    this.expressAvailability = appDataDynamicResponse.getAppDataDynamicResponse.expressAvailability;
                    this.abModeName = appDataDynamicResponse.getAppDataDynamicResponse.abModeName;
                    this.storeAvailabilityMap = appDataDynamicResponse.getAppDataDynamicResponse.storeAvailabilityMap;
                    this.specialityStoreDetailList = appDataDynamicResponse.getAppDataDynamicResponse.specialityStoresInfo;
                } else {
                    success = false;
                }
            }
            editor.putString(TIMEOUT_KEY, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault()).format(new Date())).apply();
        }
        return success;
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
        Cursor cursor = context.getContentResolver().query(AppDataDynamicAdapter.CONTENT_URI,
                AppDataDynamicAdapter.getDefaultProjection(), null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                updateInstance(context, cursor);
            }
            cursor.close();
        }
    }

    public static void reset(Context context) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(TIMEOUT_KEY).apply();
        AppDataDynamicAdapter appDataDynamicAdapter = new AppDataDynamicAdapter(context);
        appDataDynamicAdapter.delete();
        appDataDynamic = null;
    }

    @Nullable
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

    @Nullable
    public ArrayList<String> getAddToBasketPostParams() {
        return addToBasketPostParams;
    }

    @NonNull
    public String getAbModeName() {
        return abModeName == null ? "" : abModeName;
    }

    @Nullable
    public HashMap<String, String> getStoreAvailabilityMap() {
        return storeAvailabilityMap;
    }

    @Nullable
    public HashMap<String, SpecialityStoresInfoModel> getSpecialityStoreDetailList() {
        return specialityStoreDetailList;
    }
}
