package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ShipmentAction extends BaseShipmentAction implements Parcelable {
    @SerializedName(Constants.ACTION_INFO)
    private ShipmentActionInfo shipmentActionInfo;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        boolean wasShipmentActionInfoNull = shipmentActionInfo == null;
        dest.writeByte(wasShipmentActionInfoNull ? (byte) 1 : (byte) 0);
        if (!wasShipmentActionInfoNull) {
            dest.writeParcelable(shipmentActionInfo, flags);
        }
    }

    public ShipmentAction(Parcel source) {
        super(source);
        boolean wasShipmentActionInfoNull = source.readByte() == (byte) 1;
        if (!wasShipmentActionInfoNull) {
            shipmentActionInfo = source.readParcelable(ShipmentAction.class.getClassLoader());
        }
    }

    public static final Parcelable.Creator<ShipmentAction> CREATOR = new Parcelable.Creator<ShipmentAction>() {
        @Override
        public ShipmentAction createFromParcel(Parcel source) {
            return new ShipmentAction(source);
        }

        @Override
        public ShipmentAction[] newArray(int size) {
            return new ShipmentAction[size];
        }
    };

    public ShipmentActionInfo getShipmentActionInfo() {
        return shipmentActionInfo;
    }
}
