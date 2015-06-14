package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

public class ToggleShipmentAction extends BaseShipmentAction implements Parcelable {
    private String id;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
    }

    public ToggleShipmentAction(Parcel source) {
        super(source);
        id = source.readString();
    }

    public static final Parcelable.Creator<ToggleShipmentAction> CREATOR = new Parcelable.Creator<ToggleShipmentAction>() {
        @Override
        public ToggleShipmentAction createFromParcel(Parcel source) {
            return new ToggleShipmentAction(source);
        }

        @Override
        public ToggleShipmentAction[] newArray(int size) {
            return new ToggleShipmentAction[size];
        }
    };

    public String getId() {
        return id;
    }
}
