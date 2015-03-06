package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.apiservice.models.response.WalletRule;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 10/11/14.
 */
public class CurrentWalletBalance implements Parcelable {

    @SerializedName(Constants.CURRENT_BALANCE)
    public float currentBalance;

    @SerializedName(Constants.WALLET_RULE)
    public WalletRule walletRule;

    public String responseJsonStringWalletActivity;

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

    public CurrentWalletBalance(Parcel source) {
        currentBalance = source.readFloat();
        responseJsonStringWalletActivity = source.readString(); //todo
        walletRule = source.readParcelable(CurrentWalletBalance.class.getClassLoader());
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
