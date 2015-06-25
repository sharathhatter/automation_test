package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.apiservice.models.response.WalletRule;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class CurrentWalletBalance implements Parcelable {

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
    @SerializedName(Constants.CURRENT_BALANCE)
    public float currentBalance;
    @SerializedName(Constants.WALLET_RULE)
    public WalletRule walletRule;
    public String responseJsonStringWalletActivity;

    public CurrentWalletBalance(Parcel source) {
        currentBalance = source.readFloat();
        responseJsonStringWalletActivity = source.readString();
        walletRule = source.readParcelable(CurrentWalletBalance.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(currentBalance);
        dest.writeString(responseJsonStringWalletActivity);
        dest.writeParcelable(walletRule, flags);
    }
}
