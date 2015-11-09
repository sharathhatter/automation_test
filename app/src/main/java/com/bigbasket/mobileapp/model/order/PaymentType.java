package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PaymentType implements Parcelable {

    public static final Parcelable.Creator<PaymentType> CREATOR = new Parcelable.Creator<PaymentType>() {
        @Override
        public PaymentType createFromParcel(Parcel source) {
            return new PaymentType(source);
        }

        @Override
        public PaymentType[] newArray(int size) {
            return new PaymentType[size];
        }
    };
    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;
    @SerializedName(Constants.VALUE)
    private String value;
    @SerializedName(Constants.IS_SELECTED)
    private boolean isSelected;

    public PaymentType(Parcel source) {
        this.displayName = source.readString();
        this.value = source.readString();
        this.isSelected = source.readByte() == (byte) 1;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValue() {
        return value;
    }

    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(value);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }
}
