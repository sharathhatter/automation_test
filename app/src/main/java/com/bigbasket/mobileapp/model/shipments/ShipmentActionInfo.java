package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ShipmentActionInfo implements Parcelable {
    private ArrayList<ToggleShipmentAction> show;
    private ArrayList<ToggleShipmentAction> hide;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasShowNull = show == null;
        dest.writeByte(wasShowNull ? (byte) 1 : (byte) 0);
        if (!wasShowNull) {
            dest.writeTypedList(show);
        }
        boolean wasHideNull = hide == null;
        dest.writeByte(wasHideNull ? (byte) 1 : (byte) 0);
        if (!wasHideNull) {
            dest.writeTypedList(hide);
        }
    }

    public ShipmentActionInfo(Parcel source) {
        boolean wasShowNull = source.readByte() == (byte) 1;
        if (!wasShowNull) {
            show = new ArrayList<>();
            source.readTypedList(show, ToggleShipmentAction.CREATOR);
        }
        boolean wasHideNull = source.readByte() == (byte) 1;
        if (!wasHideNull) {
            hide = new ArrayList<>();
            source.readTypedList(hide, ToggleShipmentAction.CREATOR);
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

    public ArrayList<ToggleShipmentAction> getShow() {
        return show;
    }

    public ArrayList<ToggleShipmentAction> getHide() {
        return hide;
    }
}
