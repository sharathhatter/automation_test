package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 23/12/14.
 */
public class AnalyticsEngine implements Parcelable{

    @SerializedName(Constants.ENABLE_MOENGAGE)
    private boolean isMoEngageEnabled;

    @SerializedName(Constants.ENABLE_LOCALYTICS)
    private boolean isAnalyticsEnabled;

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isMoEngageEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(isAnalyticsEnabled ? (byte) 1 : (byte) 0);
    }

    public AnalyticsEngine(Parcel source) {
        isMoEngageEnabled = source.readByte() == (byte) 1;
        isAnalyticsEnabled = source.readByte() == (byte) 1;
    }

    public static final Parcelable.Creator<AnalyticsEngine> CREATOR = new Parcelable.Creator<AnalyticsEngine>() {
        @Override
        public AnalyticsEngine createFromParcel(Parcel source) {
            return new AnalyticsEngine(source);
        }

        @Override
        public AnalyticsEngine[] newArray(int size) {
            return new AnalyticsEngine[size];
        }
    };

    public boolean isMoEngageEnabled() {
        return isMoEngageEnabled;
    }

    public void setMoEngageEnabled(boolean isMoEngageEnabled) {
        this.isMoEngageEnabled = isMoEngageEnabled;
    }

    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }

    public void setAnalyticsEnabled(boolean isAnalyticsEnabled) {
        this.isAnalyticsEnabled = isAnalyticsEnabled;
    }
}
