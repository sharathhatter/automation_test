package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ShipmentActionInfo implements Parcelable {
    private ArrayList<String> show;
    private ArrayList<String> hide;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasShowNull = show == null;
        dest.writeByte(wasShowNull ? (byte) 1 : (byte) 0);
        if (!wasShowNull) {
            dest.writeStringList(show);
        }
        boolean wasHideNull = hide == null;
        dest.writeByte(wasHideNull ? (byte) 1 : (byte) 0);
        if (!wasHideNull) {
            dest.writeStringList(hide);
        }
    }

    public ShipmentActionInfo(Parcel source) {
        boolean wasShowNull = source.readByte() == (byte) 1;
        if (!wasShowNull) {
            show = new ArrayList<>();
            source.readStringList(show);
        }
        boolean wasHideNull = source.readByte() == (byte) 1;
        if (!wasHideNull) {
            hide = new ArrayList<>();
            source.readStringList(hide);
        }
    }

    public static final Parcelable.Creator<ShipmentActionInfo> CREATOR = new Parcelable.Creator<ShipmentActionInfo>() {
        @Override
        public ShipmentActionInfo createFromParcel(Parcel source) {
            return new ShipmentActionInfo(source);
        }

        @Override
        public ShipmentActionInfo[] newArray(int size) {
            return new ShipmentActionInfo[size];
        }
    };

    public ArrayList<String> getShow() {
        return show;
    }

    public ArrayList<String> getHide() {
        return hide;
    }
}
