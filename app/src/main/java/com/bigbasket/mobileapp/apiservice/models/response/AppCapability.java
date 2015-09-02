package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class AppCapability implements Parcelable {

    public static final Parcelable.Creator<AppCapability> CREATOR = new Parcelable.Creator<AppCapability>() {
        @Override
        public AppCapability createFromParcel(Parcel source) {
            return new AppCapability(source);
        }

        @Override
        public AppCapability[] newArray(int size) {
            return new AppCapability[size];
        }
    };
    @SerializedName(Constants.ENABLE_MOENGAGE)
    private boolean isMoEngageEnabled;
    @SerializedName(Constants.ENABLE_LOCALYTICS)
    private boolean isAnalyticsEnabled;
    @SerializedName(Constants.ENABLE_FB_LOGGER)
    private boolean isFBLoggerEnabled;
    @SerializedName(Constants.IS_MULTICITY_ENABLED)
    private boolean isMultiCityEnabled;

    public AppCapability(Parcel source) {
        isMoEngageEnabled = source.readByte() == (byte) 1;
        isAnalyticsEnabled = source.readByte() == (byte) 1;
        isFBLoggerEnabled = source.readByte() == (byte) 1;
        isMultiCityEnabled = source.readByte() == (byte) 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isMoEngageEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(isAnalyticsEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(isFBLoggerEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(isMultiCityEnabled ? (byte) 1 : (byte) 0);
    }

    public boolean isMoEngageEnabled() {
        return isMoEngageEnabled;
    }

    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }


    public boolean isFBLoggerEnabled() {
        return isFBLoggerEnabled;
    }

    public boolean isMultiCityEnabled() {
        return isMultiCityEnabled;
    }
}
