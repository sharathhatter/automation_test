package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

public class SlotDisplay implements Parcelable {
    private String date;
    private String time;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasDateNull = date == null;
        dest.writeByte(wasDateNull ? (byte) 1 : (byte) 0);
        if (!wasDateNull) {
            dest.writeString(date);
        }
        dest.writeString(time);
    }

    public SlotDisplay(Parcel source) {
        boolean wasDateNull = source.readByte() == (byte) 1;
        if (!wasDateNull) {
            date = source.readString();
        }
        time = source.readString();
    }

    public static final Parcelable.Creator<SlotDisplay> CREATOR = new Parcelable.Creator<SlotDisplay>() {
        @Override
        public SlotDisplay createFromParcel(Parcel source) {
            return new SlotDisplay(source);
        }

        @Override
        public SlotDisplay[] newArray(int size) {
            return new SlotDisplay[size];
        }
    };

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
