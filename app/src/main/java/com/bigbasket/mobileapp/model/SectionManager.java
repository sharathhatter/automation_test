package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SectionManager {
    public static final String HOME_PAGE = "home-page";
    public static final String MAIN_MENU = "main-menu";

    private Context context;
    private String preferenceKey;

    public SectionManager(Context context, String preferenceKey) {
        this.context = context;
        this.preferenceKey = preferenceKey;
    }

    public static ArrayList<String> getAllSectionPreferenceKeys() {
        ArrayList<String> sectionPreferenceKeys = new ArrayList<>();
        sectionPreferenceKeys.add(HOME_PAGE);
        sectionPreferenceKeys.add(MAIN_MENU);
        return sectionPreferenceKeys;
    }

    public static void clearAllSectionData(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String preferenceKey : getAllSectionPreferenceKeys()) {
            editor.remove(preferenceKey);
            editor.remove(preferenceKey + "_time");
        }
        editor.commit();
    }

    public SectionData getStoredSectionData() {
        return getStoredSectionData(false);
    }

    public SectionData getStoredSectionData(boolean ignoreStale) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String storedSectionJson = preferences.getString(preferenceKey, null);
        if (!TextUtils.isEmpty(storedSectionJson)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault());
            String createdOn = preferences.getString(preferenceKey + "_time", null);
            if (ignoreStale || !isStale(createdOn, dateFormat)) {
                return new Gson().fromJson(storedSectionJson, SectionData.class);
            }
        }
        return null;
    }

    public void storeSectionData(SectionData sectionData) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String sectionJson = new Gson().toJson(sectionData);
        editor.putString(preferenceKey, sectionJson);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                Locale.getDefault());
        editor.putString(preferenceKey + "_time", dateFormat.format(new Date()));
        editor.commit();
    }

    private boolean isStale(String createdOn, SimpleDateFormat simpleDateFormat) {
        if (BuildConfig.DEBUG) return true;
        try {
            Date createOnDate = simpleDateFormat.parse(createdOn);
            Date now = new Date();
            long minutes = TimeUnit.MINUTES.convert(now.getTime() - createOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return minutes > Section.SECTION_TIMEOUT_IN_MINUTES;
        } catch (ParseException e) {
            return true;
        }
    }
}
