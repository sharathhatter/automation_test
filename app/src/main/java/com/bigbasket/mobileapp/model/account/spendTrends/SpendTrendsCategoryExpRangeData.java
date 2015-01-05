package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

public class SpendTrendsCategoryExpRangeData implements Parcelable {

    private String category;
    private int spent;

    public SpendTrendsCategoryExpRangeData(Parcel source) {
        category = source.readString();
        spent = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeInt(spent);
    }

    public static final Parcelable.Creator<SpendTrendsCategoryExpRangeData> CREATOR = new Parcelable.Creator<SpendTrendsCategoryExpRangeData>() {
        @Override
        public SpendTrendsCategoryExpRangeData createFromParcel(Parcel source) {
            return new SpendTrendsCategoryExpRangeData(source);
        }

        @Override
        public SpendTrendsCategoryExpRangeData[] newArray(int size) {
            return new SpendTrendsCategoryExpRangeData[size];
        }
    };

    public String getCategory() {
        return category;
    }

    public int getSpent() {
        return spent;
    }
}
