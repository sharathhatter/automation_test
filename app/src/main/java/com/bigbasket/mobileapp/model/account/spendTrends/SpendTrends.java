package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpendTrends implements Parcelable {

    @SerializedName(Constants.DEFAULT_RANGE)
    private SpendTrendsDateRange defaultRange;

    @SerializedName(Constants.SPENT_SAVED)
    private ArrayList<SpendTrendsSpentSavedRangeInfo> spentSavedRangeInfos;

    @SerializedName(Constants.CATEGORY_SPENT)
    private ArrayList<SpendTrendsCategoryExpRangeInfo> categoryExpRangeInfos;

    @SerializedName(Constants.DATE_RANGE_MAP)
    private ArrayList<SpendTrendsDateRange> dateRanges;

    @SerializedName(Constants.TOP_CAT_MAP)
    private HashMap<String, String> topCategoryNameSlugMap;

    private HashMap<String, HashMap<String, SpendTrendSummary>> summary;

    public SpendTrends(Parcel source) {
        defaultRange = source.readParcelable(SpendTrends.class.getClassLoader());
        spentSavedRangeInfos = new ArrayList<>();
        source.readTypedList(spentSavedRangeInfos, SpendTrendsSpentSavedRangeInfo.CREATOR);
        categoryExpRangeInfos = new ArrayList<>();
        source.readTypedList(categoryExpRangeInfos, SpendTrendsCategoryExpRangeInfo.CREATOR);
        dateRanges = new ArrayList<>();
        source.readTypedList(dateRanges, SpendTrendsDateRange.CREATOR);
        int mapSize = source.readInt();
        topCategoryNameSlugMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            topCategoryNameSlugMap.put(source.readString(), source.readString());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(defaultRange, flags);
        dest.writeTypedList(spentSavedRangeInfos);
        dest.writeTypedList(categoryExpRangeInfos);
        dest.writeTypedList(dateRanges);
        dest.writeInt(topCategoryNameSlugMap.size());
        for (Map.Entry<String, String> topCateNameSlug : topCategoryNameSlugMap.entrySet()) {
            dest.writeString(topCateNameSlug.getKey());
            dest.writeString(topCateNameSlug.getValue());
        }
    }

    public static final Parcelable.Creator<SpendTrends> CREATOR = new Parcelable.Creator<SpendTrends>() {
        @Override
        public SpendTrends createFromParcel(Parcel source) {
            return new SpendTrends(source);
        }

        @Override
        public SpendTrends[] newArray(int size) {
            return new SpendTrends[size];
        }
    };

    public SpendTrendsDateRange getDefaultRange() {
        return defaultRange;
    }

    public ArrayList<SpendTrendsSpentSavedRangeInfo> getSpentSavedRangeInfos() {
        return spentSavedRangeInfos;
    }

    public ArrayList<SpendTrendsCategoryExpRangeInfo> getCategoryExpRangeInfos() {
        return categoryExpRangeInfos;
    }

    public HashMap<String, String> getTopCategoryNameSlugMap() {
        return topCategoryNameSlugMap;
    }

    public ArrayList<SpendTrendsDateRange> getDateRanges() {
        return dateRanges;
    }

    public HashMap<String, HashMap<String, SpendTrendSummary>> getSummary() {
        return summary;
    }
}
