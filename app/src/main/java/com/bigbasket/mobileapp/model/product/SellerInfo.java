package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 4/9/15.
 */
public class SellerInfo implements Parcelable{

    @SerializedName(Constants.TYPE)
    private String type;

    @SerializedName(Constants.STORE_ID)
    private String storeId;

    @SerializedName(Constants.AVAILABILITY)
    private String availability;

    @SerializedName(Constants.AVAILABILITY_INFO_ID)
    private String availabilityInfoId;


    @Override
    public int describeContents() {
        return 0;
    }

    public SellerInfo(Parcel source){
        type = source.readString();
        storeId = source.readString();
        availability = source.readString();
        availabilityInfoId = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(type);
        dest.writeString(storeId);
        dest.writeString(availability);
        dest.writeString(availabilityInfoId);
    }

    public static final Parcelable.Creator<SellerInfo> CREATOR = new Parcelable.Creator<SellerInfo>() {
        @Override
        public SellerInfo createFromParcel(Parcel source) {
            return new SellerInfo(source);
        }

        @Override
        public SellerInfo[] newArray(int size) {
            return new SellerInfo[size];
        }
    };
}
