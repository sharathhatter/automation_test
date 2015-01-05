package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

public class SpendTrendSummary implements Parcelable {

    private int range;
    private int saved;
    private int spent;

    public int getRange() {
        return range;
    }

    public int getSaved() {
        return saved;
    }

    public int getSpent() {
        return spent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(range);
        dest.writeInt(saved);
        dest.writeInt(spent);
    }

    public SpendTrendSummary(Parcel source) {
        range = source.readInt();
        saved = source.readInt();
        spent = source.readInt();
    }

    public static final Parcelable.Creator<SpendTrendSummary> CREATOR = new Parcelable.Creator<SpendTrendSummary>() {
        @Override
        public SpendTrendSummary createFromParcel(Parcel source) {
            return new SpendTrendSummary(source);
        }

        @Override
        public SpendTrendSummary[] newArray(int size) {
            return new SpendTrendSummary[size];
        }
    };
}
