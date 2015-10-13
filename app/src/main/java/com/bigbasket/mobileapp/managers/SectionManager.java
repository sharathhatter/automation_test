package com.bigbasket.mobileapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
    public static final String DISCOUNT_PAGE = "Discount Page";


    public static final String TIME_KEY = "_time";
    public static final String DURATION_KEY = "_duration";

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
            editor.remove(preferenceKey + TIME_KEY);
            editor.remove(preferenceKey + DURATION_KEY);
        }
        editor.apply();
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
            String createdOn = preferences.getString(preferenceKey + TIME_KEY, null);
            int cacheDuration = preferences.getInt(preferenceKey + DURATION_KEY,
                    Section.DEFAULT_SECTION_TIMEOUT_IN_MINUTES);
            if (ignoreStale || !isStale(createdOn, dateFormat, cacheDuration)) {
                return new Gson().fromJson(storedSectionJson, SectionData.class);
            }
        }
        return null;
    }

    public void storeSectionData(@Nullable SectionData sectionData, int cacheDuration) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (sectionData == null) {
            editor.remove(preferenceKey);
            editor.remove(preferenceKey + TIME_KEY);
            editor.remove(preferenceKey + DURATION_KEY);
        } else {
            String sectionJson;
            try {
                sectionJson = new Gson().toJson(sectionData);
            } catch (OutOfMemoryError e) {
                System.gc();
                return;
            }
            editor.putString(preferenceKey, sectionJson);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault());
            editor.putString(preferenceKey + TIME_KEY, dateFormat.format(new Date()));
            editor.putInt(preferenceKey + DURATION_KEY, cacheDuration);
        }
        editor.apply();
    }

    private boolean isStale(String createdOn, SimpleDateFormat simpleDateFormat, int duration) {
        try {
            Date createOnDate = simpleDateFormat.parse(createdOn);
            Date now = new Date();
            long minutes = TimeUnit.MINUTES.convert(now.getTime() - createOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return minutes > duration;
        } catch (ParseException e) {
            return true;
        }
    }
}
