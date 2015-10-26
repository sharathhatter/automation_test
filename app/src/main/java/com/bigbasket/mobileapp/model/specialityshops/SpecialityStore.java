package com.bigbasket.mobileapp.model.specialityshops;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SpecialityStore implements Parcelable {

    public static final Creator<SpecialityStore> CREATOR = new Creator<SpecialityStore>() {
        @Override
        public SpecialityStore createFromParcel(Parcel in) {
            return new SpecialityStore(in);
        }

        @Override
        public SpecialityStore[] newArray(int size) {
            return new SpecialityStore[size];
        }
    };

    @SerializedName(Constants.STORE_IMG)
    private String storeImg;
    @SerializedName(Constants.NAME)
    private String storeName;
    @SerializedName(Constants.LOCATION)
    private String storeLocation;
    @SerializedName(Constants.DELIVERY_TIME)
    private String storeDeliveryTime;
    @SerializedName(Constants.STORE_TIMINGS)
    private String storeTimings;
    @SerializedName(Constants.DESTINATION)
    private DestinationInfo destinationInfo;

    public SpecialityStore(Parcel source) {
        boolean _wasImgNull = source.readByte() == (byte) 1;
        if (!_wasImgNull) {
            this.storeImg = source.readString();
        }
        boolean _wasStoreNameNull = source.readByte() == (byte) 1;
        if (!_wasStoreNameNull) {
            this.storeName = source.readString();
        }
        boolean _wasStoreLocNull = source.readByte() == (byte) 1;
        if (!_wasStoreLocNull) {
            this.storeLocation = source.readString();
        }
        boolean _wasStoreDelTimeNull = source.readByte() == (byte) 1;
        if (!_wasStoreDelTimeNull) {
            this.storeDeliveryTime = source.readString();
        }
        boolean _wasStoreTimingsNull = source.readByte() == (byte) 1;
        if (!_wasStoreTimingsNull) {
            this.storeTimings = source.readString();
        }
        boolean wasDestNull = source.readByte() == (byte) 1;
        if (!wasDestNull) {
            destinationInfo = source.readParcelable(SpecialityStore.class.getClassLoader());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(storeImg);
        dest.writeString(storeName);
        dest.writeString(storeLocation);
        dest.writeString(storeDeliveryTime);
        dest.writeString(storeTimings);
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public String getStoreDeliveryTime() {
        return storeDeliveryTime;
    }

    public String getStoreTimings() {
        return storeTimings;
    }

    public String getStoreImg() {
        return storeImg;
    }

    public DestinationInfo getDestinationInfo() {
        return destinationInfo;
    }
}
