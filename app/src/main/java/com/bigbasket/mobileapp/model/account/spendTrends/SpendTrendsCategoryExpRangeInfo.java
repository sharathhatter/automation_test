package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SpendTrendsCategoryExpRangeInfo extends SpendTrendsDateRange {

    @SerializedName(Constants.RANGE_DATA)
    private ArrayList<SpendTrendsCategoryExpRangeData> spendTrendsCategoryExpRangeDataList;

    public SpendTrendsCategoryExpRangeInfo(Parcel source) {
        super(source);
        spendTrendsCategoryExpRangeDataList = new ArrayList<>();
        source.readTypedList(spendTrendsCategoryExpRangeDataList, SpendTrendsCategoryExpRangeData.CREATOR);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(spendTrendsCategoryExpRangeDataList);
    }

    public static final Parcelable.Creator<SpendTrendsCategoryExpRangeInfo> CREATOR = new Parcelable.Creator<SpendTrendsCategoryExpRangeInfo>() {
        @Override
        public SpendTrendsCategoryExpRangeInfo createFromParcel(Parcel source) {
            return new SpendTrendsCategoryExpRangeInfo(source);
        }

        @Override
        public SpendTrendsCategoryExpRangeInfo[] newArray(int size) {
            return new SpendTrendsCategoryExpRangeInfo[size];
        }
    };

    public ArrayList<SpendTrendsCategoryExpRangeData> getSpendTrendsCategoryExpRangeDataList() {
        return spendTrendsCategoryExpRangeDataList;
    }

    public static SpendTrendsCategoryExpRangeInfo getSelectedCategoryExpRangeInfo(
            ArrayList<SpendTrendsCategoryExpRangeInfo> spendTrendsCategoryExpRangeInfos, SpendTrendsDateRange spendTrendsDateRange) {
        for (SpendTrendsCategoryExpRangeInfo categoryExpRangeInfo : spendTrendsCategoryExpRangeInfos) {
            if (categoryExpRangeInfo.getRangeVal() == spendTrendsDateRange.getRangeVal()) {
                return categoryExpRangeInfo;
            }
        }
        return null;
    }

    public static ArrayList<SpendTrendsCategoryExpRangeData> getFilteredCategoryExpRangeData(
            SpendTrendsCategoryExpRangeInfo categoryExpRangeInfo, String categoryName) {
        ArrayList<SpendTrendsCategoryExpRangeData> categoryExpRangeDatas = new ArrayList<>();
        if (categoryName.equals(Constants.ALL_CATEGORIES)) {
            return categoryExpRangeInfo.getSpendTrendsCategoryExpRangeDataList();
        }
        for (SpendTrendsCategoryExpRangeData categoryExpRangeData : categoryExpRangeInfo.getSpendTrendsCategoryExpRangeDataList()) {
            if (categoryExpRangeData.getCategory().equals(categoryName)) {
                categoryExpRangeDatas.add(categoryExpRangeData);
            }
        }
        return categoryExpRangeDatas;
    }
}
