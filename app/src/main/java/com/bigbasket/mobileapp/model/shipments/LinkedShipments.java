package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LinkedShipments implements Parcelable {
    @SerializedName(Constants.SKU_LIST)
    private ArrayList<Product> skuList;
    private int count;
    private String total;
    @SerializedName(Constants.SHIPMENT_ID)
    private int shipmentId;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasSkuListNull = skuList == null;
        dest.writeByte(wasSkuListNull ? (byte) 1 : (byte) 0);
        if (!wasSkuListNull) {
            dest.writeTypedList(skuList);
        }
        dest.writeInt(count);
        dest.writeString(total);
        dest.writeInt(shipmentId);
    }

    public LinkedShipments(Parcel source) {
        boolean wasSkuListNull = source.readByte() == (byte) 1;
        if (!wasSkuListNull) {
            skuList = source.createTypedArrayList(Product.CREATOR);
        }
        count = source.readInt();
        total = source.readString();
        shipmentId = source.readInt();
    }

    public static final Parcelable.Creator<LinkedShipments> CREATOR = new Parcelable.Creator<LinkedShipments>() {
        @Override
        public LinkedShipments createFromParcel(Parcel source) {
            return new LinkedShipments(source);
        }

        @Override
        public LinkedShipments[] newArray(int size) {
            return new LinkedShipments[size];
        }
    };

    public ArrayList<Product> getSkuList() {
        return skuList;
    }

    public int getCount() {
        return count;
    }

    public String getTotal() {
        return total;
    }

    public int getShipmentId() {
        return shipmentId;
    }
}
