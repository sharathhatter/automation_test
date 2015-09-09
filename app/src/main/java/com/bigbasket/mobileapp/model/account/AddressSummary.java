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

    public AddressSummary(Parcel source) {
        this.id = source.readString();
        this.area = source.readString();
        this.cityName = source.readString();
        this.cityId = source.readInt();
        this.pincode = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
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
}
