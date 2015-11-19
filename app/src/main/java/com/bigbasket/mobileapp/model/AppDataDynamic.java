package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
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
                                      ArrayList<String> addToBasketPostParams,
                                      ArrayList<AddressSummary> addressSummaries,
                                      boolean isContextualMode,
                                      String expressAvailability,
                                      String abModeName,
                                      HashMap<String, String> storeAvailabilityMap) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        if (addressSummaries != null) {
            String addressJson = gson.toJson(addressSummaries);
            editor.putString(Constants.ADDRESSES, addressJson);
        }
        if (addToBasketPostParams != null) {
            String addToBasketPostParamsJson = gson.toJson(addToBasketPostParams);
            editor.putString(Constants.ADD_TO_BASKET_POST_PARAMS, addToBasketPostParamsJson);
        }
        if (abModeName != null) {
            editor.putString(Constants.MODE_NAME, abModeName);
        }
        if (storeAvailabilityMap != null) {
            String storeJson = gson.toJson(storeAvailabilityMap);
            editor.putString(Constants.STORE_AVAILABILITY_MAP, storeJson);
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
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(addressJson)) {
            Type collectionType = new TypeToken<Collection<AddressSummary>>() {
            }.getType();
            this.addressSummaries = gson.fromJson(addressJson, collectionType);
        }

        this.isContextualMode = preferences.getBoolean(Constants.IS_CONTEXTUAL_MODE, false);
        this.expressAvailability = preferences.getString(Constants.EXPRESS_AVAILABILITY, null);
        String addToBasketPostParamsJson = preferences.getString(Constants.ADD_TO_BASKET_POST_PARAMS, null);
        if (addToBasketPostParamsJson != null) {
            Type collectionType = new TypeToken<Collection<String>>() {
            }.getType();
            this.addToBasketPostParams = gson.fromJson(addToBasketPostParamsJson, collectionType);
        }
        this.abModeName = preferences.getString(Constants.MODE_NAME, null);
        String storeJson = preferences.getString(Constants.STORE_AVAILABILITY_MAP, null);
        if (storeJson != null) {
            Type collectionType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            this.storeAvailabilityMap = gson.fromJson(storeJson, collectionType);
        }

    }

    public static void reset(Context context) {
        if (context == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(Constants.ADDRESSES)
                .remove(Constants.IS_CONTEXTUAL_MODE)
                .remove(TIMEOUT_KEY)
                .remove(Constants.EXPRESS_AVAILABILITY)
                .remove(Constants.MODE_NAME)
                .remove(Constants.ADD_TO_BASKET_POST_PARAMS)
                .remove(Constants.STORE_AVAILABILITY_MAP)
                .apply();
        SpecialityStorePreference.reset(context);
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

}
