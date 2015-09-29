package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class AddressSummary implements Parcelable {

    public static final Parcelable.Creator<AddressSummary> CREATOR = new Parcelable.Creator<AddressSummary>() {
        @Override
        public AddressSummary createFromParcel(Parcel source) {
            return new AddressSummary(source);
        }

        @Override
        public AddressSummary[] newArray(int size) {
            return new AddressSummary[size];
        }
    };

    @SerializedName(Constants.ADDR_NICK)
    private String addressNickName;
    @SerializedName(Constants.ID)
    private String id;
    @SerializedName(Constants.AREA)
    private String area;
    @SerializedName(Constants.CITY_NAME)
    private String cityName;
    @SerializedName(Constants.CITY_ID)
    private int cityId;
    @SerializedName(Constants.PIN)
    private String pincode;
    @SerializedName(Constants.LAT)
    private double latitude;
    @SerializedName(Constants.LNG)
    private double longitude;
    @SerializedName(Constants.IS_PARTIAL)
    private boolean isPartial;
    private String slot;

    public AddressSummary(Parcel source) {
        this.id = source.readString();
        this.area = source.readString();
        this.cityName = source.readString();
        this.cityId = source.readInt();
        this.pincode = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
        boolean isAddressNickNameNull = source.readByte() == (byte) 1;
        if (!isAddressNickNameNull) {
            this.addressNickName = source.readString();
        }
        this.isPartial = source.readByte() == (byte) 1;
        boolean isSlotNull = source.readByte() == (byte) 1;
        if (!isSlotNull) {
            this.slot = source.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(area);
        dest.writeString(cityName);
        dest.writeInt(cityId);
        dest.writeString(pincode);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        boolean isAddressNickNameNull = addressNickName == null;
        dest.writeByte(isAddressNickNameNull ? (byte) 1 : (byte) 0);
        if (!isAddressNickNameNull) {
            dest.writeString(addressNickName);
        }
        dest.writeByte(isPartial ? (byte) 1 : (byte) 0);
        boolean isSlotNull = slot == null;
        dest.writeByte(isSlotNull ? (byte) 1 : (byte) 0);
        if (!isSlotNull) {
            dest.writeString(slot);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return ((!TextUtils.isEmpty(area) ? area + "\n" : "") +
                (!TextUtils.isEmpty(cityName) ? cityName : "") +
                (!TextUtils.isEmpty(pincode) ? "- " + pincode : ""));
    }

    public String toStringSameLine() {
        return ((!TextUtils.isEmpty(area) ? area + ", " : "") +
                (!TextUtils.isEmpty(cityName) ? cityName : "") +
                (!TextUtils.isEmpty(pincode) ? "- " + pincode : ""));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCityName() {
        return cityName;
    }

    public String getPincode() {
        return pincode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddressNickName() {
        return addressNickName;
    }

    public String getSlot() {
        return slot;
    }

    public boolean isPartial() {
        return isPartial;
    }
}
