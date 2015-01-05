package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SpendTrendsDateRange implements Parcelable {

    @SerializedName(Constants.RANGE_NAME)
    private String rangeName;

    @SerializedName(Constants.RANGE_VAL)
    private int rangeVal;

    public SpendTrendsDateRange(Parcel source) {
        rangeName = source.readString();
        rangeVal = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rangeName);
        dest.writeInt(rangeVal);
    }

    public static final Parcelable.Creator<SpendTrendsDateRange> CREATOR = new Parcelable.Creator<SpendTrendsDateRange>() {
        @Override
        public SpendTrendsDateRange createFromParcel(Parcel source) {
            return new SpendTrendsDateRange(source);
        }

        @Override
        public SpendTrendsDateRange[] newArray(int size) {
            return new SpendTrendsDateRange[size];
        }
    };

    public String getRangeName() {
        return rangeName;
    }

    public int getRangeVal() {
        return rangeVal;
    }

    @Override
    public String toString() {
        return rangeName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SpendTrendsDateRange) {
            return this.rangeVal == ((SpendTrendsDateRange) o).getRangeVal();
        }
        return super.equals(o);
    }

    public static int getSelectedIndex(ArrayList<SpendTrendsDateRange> spendTrendsDateRanges, SpendTrendsDateRange defaultSpendTrendsDateRange) {
        for (int i = 0; i < spendTrendsDateRanges.size(); i++) {
            if (spendTrendsDateRanges.get(i) == defaultSpendTrendsDateRange) {
                return i;
            }
        }
        return 0;
    }
}
