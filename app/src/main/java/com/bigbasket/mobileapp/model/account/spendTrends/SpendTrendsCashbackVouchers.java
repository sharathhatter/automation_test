package com.bigbasket.mobileapp.model.account.spendTrends;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SpendTrendsCashbackVouchers implements Parcelable {

    @SerializedName(Constants.MSG)
    private String message;
    private double value;

    public SpendTrendsCashbackVouchers(Parcel source) {
        message = source.readString();
        value = source.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeDouble(value);
    }

    public static final Parcelable.Creator<SpendTrendsCashbackVouchers> CREATOR = new Parcelable.Creator<SpendTrendsCashbackVouchers>() {
        @Override
        public SpendTrendsCashbackVouchers createFromParcel(Parcel source) {
            return new SpendTrendsCashbackVouchers(source);
        }

        @Override
        public SpendTrendsCashbackVouchers[] newArray(int size) {
            return new SpendTrendsCashbackVouchers[size];
        }
    };

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
    }
}
