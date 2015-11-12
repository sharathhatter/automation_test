package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by jugal on 10/11/15.
 */
public class SpecialityStorePreference {

    private static final String SPECIALITY_STORE_PREF_FILE = "speciality_store_details";

    private static SharedPreferences getSpecialityStorePreferences(Context context) {
        return context.getSharedPreferences(SPECIALITY_STORE_PREF_FILE, Context.MODE_PRIVATE);
    }


    public static void saveSpecialityStoreDetailList(Context context, HashMap<String, SpecialityStoresInfoModel> specialityStoresInfo) {
        String storeInfoJson = new Gson().toJson(specialityStoresInfo);
        getSpecialityStorePreferences(context.getApplicationContext())
                .edit().putString(Constants.SPECIALITY_STORES_INFO, storeInfoJson).apply();
    }

    public static void reset(Context context) {
        getSpecialityStorePreferences(context.getApplicationContext())
                .edit().remove(Constants.SPECIALITY_STORES_INFO).apply();
    }

    @Nullable
    public static HashMap<String, SpecialityStoresInfoModel> getSpecialityStoreDetailList(Context context) {
        String storeInfoJson =  getSpecialityStorePreferences(context.getApplicationContext()).getString(Constants.SPECIALITY_STORES_INFO, null);
        if (storeInfoJson != null) {
            Type collectionType = new TypeToken<HashMap<String, SpecialityStoresInfoModel>>() {
            }.getType();
            return new Gson().fromJson(storeInfoJson, collectionType);
        }
        return null;
    }
}
