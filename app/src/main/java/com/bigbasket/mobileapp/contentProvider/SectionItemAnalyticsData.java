package com.bigbasket.mobileapp.contentProvider;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider;

import java.util.Calendar;

/**
 * Created by muniraju on 17/12/15.
 */
public class SectionItemAnalyticsData {

    public static final String TABLE_NAME = "section_item_analytics_data";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(
            DatabaseContentProvider.CONTENT_URI_PREFIX, TABLE_NAME);
    public static final String MIME_TYPE_DIR =
            "vnd.android.cursor.dir/com.bigbasket.mobileapp.section_item_analytics_data";
    public static final String MIME_TYPE_ITEM =
            "vnd.android.cursor.item/com.bigbasket.mobileapp.section_item_analytics_data";

    public static final String ID = BaseColumns._ID;
    public static final String SECTION_ID = "section_id";
    public static final String CITY_ID = "city_id";
    public static final String CLICKS = "clicks";
    public static final String IMPRESSIONS = "impressions";
    public static final String ANALYTICS_ATTRS = "analytics_attr";
    //DATE in epochtime format, time is only date part is stored
    public static final String DATE = "date";

    public static final String[] PROJECTION = new String[]{
            ID,
            SECTION_ID,
            CITY_ID,
            CLICKS,
            IMPRESSIONS,
            ANALYTICS_ATTRS,
            DATE
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_SECTION_ID = COLUMN_ID + 1;
    public static final int COLUMN_CITY_ID = COLUMN_SECTION_ID + 1;
    public static final int COLUMN_CLICKS = COLUMN_CITY_ID + 1;
    public static final int COLUMN_IMPRESSIONS = COLUMN_CLICKS + 1;
    public static final int COLUMN_ANALYTICS_ATTRS = COLUMN_IMPRESSIONS + 1;
    public static final int COLUMN_DATE = COLUMN_ANALYTICS_ATTRS + 1;

    private long id;
    private String sectionId;
    private String cityId;
    private int clicks;
    private int impressions;
    private String analyticsAttrs;
    private long date;

    public SectionItemAnalyticsData() {
    }

    public SectionItemAnalyticsData(Cursor cursor) {
        id = cursor.getLong(COLUMN_ID);
        sectionId = cursor.getString(COLUMN_SECTION_ID);
        cityId = cursor.getString(COLUMN_CITY_ID);
        clicks = cursor.getInt(COLUMN_CLICKS);
        impressions = cursor.getInt(COLUMN_IMPRESSIONS);
        analyticsAttrs = cursor.getString(COLUMN_ANALYTICS_ATTRS);
        date = cursor.getLong(COLUMN_DATE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public String getAnalyticsAttrs() {
        return analyticsAttrs;
    }

    public void setAnalyticsAttrs(String analyticsAttrs) {
        this.analyticsAttrs = analyticsAttrs;
    }

    public long getDate() {
        return date;
    }

    public static long dateNow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

}
