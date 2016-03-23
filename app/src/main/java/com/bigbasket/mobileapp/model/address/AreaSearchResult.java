package com.bigbasket.mobileapp.model.address;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by muniraju on 29/02/16.
 */
public class AreaSearchResult implements Parcelable {

    @SerializedName("type")
    private String type;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("pincode")
    private String pincode;

    @SerializedName("area")
    private String area;

    @SerializedName("street")
    private String street;

    @SerializedName("landmark")
    private String landmark;

    @SerializedName("other_area")
    private boolean otherArea;

    @SerializedName(Constants.LOCATION)
    private String[] location;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public boolean isOtherArea() {
        return otherArea;
    }

    public void setOtherArea(boolean otherArea) {
        this.otherArea = otherArea;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.pincode);
        dest.writeString(this.area);
        dest.writeString(this.street);
        dest.writeString(this.landmark);
        dest.writeStringArray(this.location);
    }

    public AreaSearchResult() {
    }

    protected AreaSearchResult(Parcel in) {
        this.displayName = in.readString();
        this.pincode = in.readString();
        this.area = in.readString();
        this.street = in.readString();
        this.landmark = in.readString();
        this.location = in.createStringArray();
    }

    public static final Parcelable.Creator<AreaSearchResult> CREATOR = new Parcelable.Creator<AreaSearchResult>() {
        public AreaSearchResult createFromParcel(Parcel source) {
            return new AreaSearchResult(source);
        }

        public AreaSearchResult[] newArray(int size) {
            return new AreaSearchResult[size];
        }
    };
}
