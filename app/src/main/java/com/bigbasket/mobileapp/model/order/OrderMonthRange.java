package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderMonthRange implements Parcelable {

    private int value;

    @SerializedName(Constants.DISPLAY_VALUE)
    private String displayValue;

    public OrderMonthRange(int value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(value);
        dest.writeString(displayValue);
    }

    public OrderMonthRange(Parcel source) {
        value = source.readInt();
        displayValue = source.readString();
    }

    public static final Parcelable.Creator<OrderMonthRange> CREATOR = new Parcelable.Creator<OrderMonthRange>() {
        @Override
        public OrderMonthRange createFromParcel(Parcel source) {
            return new OrderMonthRange(source);
        }

        @Override
        public OrderMonthRange[] newArray(int size) {
            return new OrderMonthRange[size];
        }
    };

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return this.displayValue;
    }

    public static int getSelectedIndex(ArrayList<OrderMonthRange> orderMonthRanges, int selectedValue) {
        int idx = 0;
        if (orderMonthRanges == null || orderMonthRanges.size() == 0) return idx;
        for (int i = 0; i < orderMonthRanges.size(); i++) {
            if (orderMonthRanges.get(i).getValue() == selectedValue) {
                return i;
            }
        }
        return idx;
    }
}
