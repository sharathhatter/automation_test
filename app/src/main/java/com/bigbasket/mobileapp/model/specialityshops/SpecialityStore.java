package com.bigbasket.mobileapp.model.specialityshops;

import android.os.Parcel;
import android.os.Parcelable;

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

    @SerializedName(Constants.IMG_ANME)
    private String storeImg;
    @SerializedName(Constants.NAME)
    private String storeName;
    @SerializedName(Constants.LOCATION)
    private String storeLocation;
    @SerializedName(Constants.DELIVERY_TIME)
    private String storeDeliveryTime;
    @SerializedName(Constants.OP_TIME)
    private String storeOpTime;

    public SpecialityStore(String storeImg, String storeName, String storeLocation, String storeDeliveryTime, String storeOpTime) {
        this.storeImg = storeImg;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
        this.storeDeliveryTime = storeDeliveryTime;
        this.storeOpTime = storeOpTime;
    }

    public SpecialityStore(Parcel source) {
        this.storeImg = source.readString();
        this.storeName = source.readString();
        this.storeLocation = source.readString();
        this.storeDeliveryTime = source.readString();
        this.storeOpTime = source.readString();
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
        dest.writeString(storeOpTime);
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

    public String getStoreOpTime() {
        return storeOpTime;
    }

    public String getStoreImg() {
        return storeImg;
    }
}
