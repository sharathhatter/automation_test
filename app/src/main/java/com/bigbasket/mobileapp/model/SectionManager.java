package com.bigbasket.mobileapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SectionManager {
    public static final String HOME_PAGE_SECTION = "homePageSection";

    private Context context;
    private String preferenceKey;

    public static ArrayList<String> getAllSectionPreferenceKeys() {
        ArrayList<String> sectionPreferenceKeys = new ArrayList<>();
        sectionPreferenceKeys.add(HOME_PAGE_SECTION);
        return sectionPreferenceKeys;
    }

    public SectionManager(Context context, String preferenceKey) {
        this.context = context;
        this.preferenceKey = preferenceKey;
    }

    public SectionData getStoredSectionData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String storedSectionJson = preferences.getString(preferenceKey, null);
        if (!TextUtils.isEmpty(storedSectionJson)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String createdOn = preferences.getString(preferenceKey + "_time", null);
            if (!isStale(createdOn, dateFormat)) {
                return new Gson().fromJson(storedSectionJson, SectionData.class);
            }
        }
        return null;
    }

    public void storeSectionData(SectionData sectionData) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String sectionJson = new Gson().toJson(sectionData);
        editor.putString(preferenceKey, sectionJson);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        editor.putString(preferenceKey + "_time", dateFormat.format(new Date()));
        editor.commit();
    }

    public static void clearAllSectionData(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String preferenceKey : getAllSectionPreferenceKeys()) {
            editor.remove(preferenceKey);
            editor.remove(preferenceKey + "_time");
        }
        editor.commit();
    }

    private boolean isStale(String createdOn, SimpleDateFormat simpleDateFormat) {
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
