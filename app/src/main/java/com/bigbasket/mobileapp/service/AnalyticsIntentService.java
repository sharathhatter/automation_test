package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.contentProvider.SectionItemAnalyticsData;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Analytics service to update the analytics values
 */
public class AnalyticsIntentService extends IntentService {

    private static final String ACTION_UPDATE_ANALYTICS_EVENT =
            "com.bigbasket.mobileapp.service.action.ACTION_UPDATE_ANALYTICS_EVENT";

    private static final String EXTRA_CLICK_COUNT =
            "com.bigbasket.mobileapp.analytics_service.extra.CLICK_COUNT";
    private static final String EXTRA_IMPS_COUNT =
            "com.bigbasket.mobileapp.analytics_service.extra.IMPS_COUNT";
    private static final String EXTRA_SECTION_ID =
            "com.bigbasket.mobileapp.analytics_service.extra.SECTION_ID";
    private static final String EXTRA_CITY_ID =
            "com.bigbasket.mobileapp.analytics_service.extra.CITY_ID";
    private static final String EXTRA_ANALYTICS_ATTRIBUTES =
            "com.bigbasket.mobileapp.analytics_service.extra.ANALYTICS_ATTRIBUTES";

    private final static Object sAnalyticsEventLock = new Object();

    public AnalyticsIntentService() {
        super("AnalyticsIntentService");
    }

    public static void startUpdateAnalyticsEvent(Context context, int clickCount,
                                                 int impressionsCount,
                                                 String sectionId,
                                                 String cityId,
                                                 String analyticsAttributes) {
        Intent intent = new Intent(context, AnalyticsIntentService.class);
        intent.setAction(ACTION_UPDATE_ANALYTICS_EVENT);
        intent.putExtra(EXTRA_CLICK_COUNT, clickCount);
        intent.putExtra(EXTRA_IMPS_COUNT, impressionsCount);
        intent.putExtra(EXTRA_SECTION_ID, sectionId);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        intent.putExtra(EXTRA_ANALYTICS_ATTRIBUTES, analyticsAttributes);
        context.startService(intent);
    }

    public static void startUpdateAnalyticsEvent(Context context, boolean isClickEvent,
                                                 String sectionId,
                                                 String cityId,
                                                 String analyticsAttributes) {
        if (TextUtils.isEmpty(sectionId)) {
            return;
        }
        int clicks = 0;
        int imps = 0;
        if (isClickEvent) {
            clicks++;
        } else {
            imps++;
        }
        startUpdateAnalyticsEvent(context, clicks, imps, sectionId, cityId, analyticsAttributes);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_ANALYTICS_EVENT.equals(action)) {
                handleUpdateAnalyticsEvent(intent.getIntExtra(EXTRA_CLICK_COUNT, 0),
                        intent.getIntExtra(EXTRA_IMPS_COUNT, 0),
                        intent.getStringExtra(EXTRA_SECTION_ID),
                        intent.getStringExtra(EXTRA_CITY_ID),
                        intent.getStringExtra(EXTRA_ANALYTICS_ATTRIBUTES));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleUpdateAnalyticsEvent(int clicks,
                                            int impressions,
                                            String sectionId,
                                            String cityId,
                                            String analyticsAttributes) {
        synchronized (sAnalyticsEventLock) {
            if (TextUtils.isEmpty(sectionId)) {
                return;
            }
            if (TextUtils.isEmpty(cityId)) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                cityId = preferences.getString(Constants.CITY_ID, "");
            }
            //Get the existing data for sectionId and cityId
            Cursor cursor = getContentResolver().query(SectionItemAnalyticsData.CONTENT_URI,
                    SectionItemAnalyticsData.PROJECTION,
                    SectionItemAnalyticsData.SECTION_ID + " = ? AND "
                            + SectionItemAnalyticsData.CITY_ID + " = ?",
                    new String[]{sectionId, cityId},
                    null);
            SectionItemAnalyticsData sectionItemAnalyticsData = null;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    sectionItemAnalyticsData = new SectionItemAnalyticsData(cursor);
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (sectionItemAnalyticsData != null) {
                ContentValues values = new ContentValues(3);
                values.put(SectionItemAnalyticsData.CLICKS,
                        sectionItemAnalyticsData.getClicks() + clicks);
                values.put(SectionItemAnalyticsData.IMPRESSIONS,
                        sectionItemAnalyticsData.getImpressions() + impressions);
                values.put(SectionItemAnalyticsData.ANALYTICS_ATTRS, analyticsAttributes);
                getContentResolver().update(
                        ContentUris.withAppendedId(SectionItemAnalyticsData.CONTENT_URI,
                                sectionItemAnalyticsData.getId()),
                        values, null, null);
            } else {
                ContentValues values = new ContentValues(SectionItemAnalyticsData.PROJECTION.length);
                values.put(SectionItemAnalyticsData.CLICKS, clicks);
                values.put(SectionItemAnalyticsData.IMPRESSIONS, impressions);
                values.put(SectionItemAnalyticsData.CITY_ID, cityId);
                values.put(SectionItemAnalyticsData.SECTION_ID, sectionId);
                values.put(SectionItemAnalyticsData.ANALYTICS_ATTRS, analyticsAttributes);
                getContentResolver().insert(SectionItemAnalyticsData.CONTENT_URI, values);
            }
        }
    }


    public static List<SectionItemAnalyticsData> getAnalyticsData(Context context) {

        synchronized (sAnalyticsEventLock) {
            Cursor cursor = context.getContentResolver().query(SectionItemAnalyticsData.CONTENT_URI,
                    SectionItemAnalyticsData.PROJECTION,
                    null,
                    null,
                    null);
            try {
                if (cursor != null) {
                    List<SectionItemAnalyticsData> sectionItemAnalyticsDataList =
                            new ArrayList<>(cursor.getCount());
                    while (cursor.moveToNext()) {
                        sectionItemAnalyticsDataList.add(new SectionItemAnalyticsData(cursor));
                    }
                    context.getContentResolver().delete(SectionItemAnalyticsData.CONTENT_URI,
                            null, null);
                    return sectionItemAnalyticsDataList;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

}
