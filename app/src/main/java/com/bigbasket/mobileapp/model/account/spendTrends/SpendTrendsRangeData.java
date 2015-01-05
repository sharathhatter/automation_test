package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

public class SpendTrendsRangeData extends SpendTrendsCategoryExpRangeData {

    private String month;

    private int saved;

    public SpendTrendsRangeData(Parcel source) {
        super(source);
        month = source.readString();
        saved = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(month);
        dest.writeInt(saved);
    }

    public static final Parcelable.Creator<SpendTrendsRangeData> CREATOR = new Parcelable.Creator<SpendTrendsRangeData>() {
        @Override
        public SpendTrendsRangeData createFromParcel(Parcel source) {
            return new SpendTrendsRangeData(source);
        }

        @Override
        public SpendTrendsRangeData[] newArray(int size) {
            return new SpendTrendsRangeData[size];
        }
    };

    public String getMonth() {
        return month;
    }

    public int getSaved() {
        return saved;
    }
}
