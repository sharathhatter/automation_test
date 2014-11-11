package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jugal on 10/11/14.
 */
public class CurrentWalletBalance implements Parcelable {

    private float currentBalance;
    private String responseJsonStringWalletActivity;

    public float getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(float currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getResponseJsonStringWalletActivity() {
        return responseJsonStringWalletActivity;
    }

    public void setResponseJsonStringWalletActivity(String responseJsonStringWalletActivity) {
        this.responseJsonStringWalletActivity = responseJsonStringWalletActivity;
    }

    public CurrentWalletBalance(float currentBalance, String responseJsonStringWalletActivity) {
        this.currentBalance = currentBalance;
        this.responseJsonStringWalletActivity = responseJsonStringWalletActivity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(currentBalance);
        dest.writeString(responseJsonStringWalletActivity);
    }

    public CurrentWalletBalance(Parcel source) {
        currentBalance = source.readFloat();
        responseJsonStringWalletActivity = source.readString();
    }

    public static final Parcelable.Creator<CurrentWalletBalance> CREATOR = new Parcelable.Creator<CurrentWalletBalance>() {
        @Override
        public CurrentWalletBalance createFromParcel(Parcel source) {
            return new CurrentWalletBalance(source);
        }

        @Override
        public CurrentWalletBalance[] newArray(int size) {
            return new CurrentWalletBalance[size];
        }
    };
}
