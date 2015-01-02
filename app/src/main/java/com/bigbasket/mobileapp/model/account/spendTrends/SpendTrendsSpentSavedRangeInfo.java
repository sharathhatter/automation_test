package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SpendTrendsSpentSavedRangeInfo extends SpendTrendsDateRange {


    @SerializedName(Constants.RANGE_DATA)
    private ArrayList<SpendTrendsRangeData> spendTrendsRangeDataList;

    @SerializedName(Constants.CASHBACK_VOUCHERS)
    private SpendTrendsCashbackVouchers spendTrendsCashbackVouchers;

    public SpendTrendsSpentSavedRangeInfo(Parcel source) {
        super(source);
        spendTrendsRangeDataList = new ArrayList<>();
        source.readTypedList(spendTrendsRangeDataList, SpendTrendsRangeData.CREATOR);
        spendTrendsCashbackVouchers = source.
                readParcelable(SpendTrendsCategoryExpRangeInfo.class.getClassLoader());

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(spendTrendsRangeDataList);
        dest.writeParcelable(spendTrendsCashbackVouchers, flags);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    public static final Parcelable.Creator<SpendTrendsSpentSavedRangeInfo> CREATOR = new Parcelable.Creator<SpendTrendsSpentSavedRangeInfo>() {
        @Override
        public SpendTrendsSpentSavedRangeInfo createFromParcel(Parcel source) {
            return new SpendTrendsSpentSavedRangeInfo(source);
        }

        @Override
        public SpendTrendsSpentSavedRangeInfo[] newArray(int size) {
            return new SpendTrendsSpentSavedRangeInfo[size];
        }
    };

    public ArrayList<SpendTrendsRangeData> getSpendTrendsRangeDataList() {
        return spendTrendsRangeDataList;
    }

    public SpendTrendsCashbackVouchers getSpendTrendsCashbackVouchers() {
        return spendTrendsCashbackVouchers;
    }

    public static SpendTrendsSpentSavedRangeInfo getSelectedSpentSavedRangeInfo(
            ArrayList<SpendTrendsSpentSavedRangeInfo> spendTrendsSpentSavedRangeInfos, SpendTrendsDateRange spendTrendsDateRange) {
        for (SpendTrendsSpentSavedRangeInfo spentSavedRangeInfo : spendTrendsSpentSavedRangeInfos) {
            if (spentSavedRangeInfo.getRangeVal() == spendTrendsDateRange.getRangeVal()) {
                return spentSavedRangeInfo;
            }
        }
        return null;
    }

    public static ArrayList<SpendTrendsRangeData> getFilteredSpendTrendsRangeData(SpendTrendsSpentSavedRangeInfo spentSavedRangeInfo,
                                                                                  String categoryName) {
        ArrayList<SpendTrendsRangeData> spendTrendsRangeDatas = new ArrayList<>();
        for (SpendTrendsRangeData spendTrendsRangeData : spentSavedRangeInfo.getSpendTrendsRangeDataList()) {
            if (spendTrendsRangeData.getCategory().equals(categoryName)) {
                spendTrendsRangeDatas.add(spendTrendsRangeData);
            }
        }
        return spendTrendsRangeDatas;
    }
}
