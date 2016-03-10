package com.bigbasket.mobileapp.model.wallet;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by manu on 26/2/16.
 */
public class WalletOption implements Parcelable {


    public static final Creator<WalletOption> CREATOR = new Creator<WalletOption>() {
        @Override
        public WalletOption createFromParcel(Parcel in) {
            return new WalletOption(in);
        }

        @Override
        public WalletOption[] newArray(int size) {
            return new WalletOption[size];
        }
    };

    @SerializedName(Constants.WALLET_OPTION_BALANCE)
    private String walletBalance;
    @SerializedName(Constants.WALLET_OPTION_MSG)
    private String walletMessage;
    @SerializedName(Constants.WALLET_OPTION_STATE)
    private String walletState;

    public WalletOption() {
    }

    WalletOption(Parcel source) {
        walletBalance = source.readString();
        walletMessage = source.readString();
        walletState = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(walletBalance);
        dest.writeString(walletMessage);
        dest.writeString(walletState);

    }

    public String getWalletState() {
        return walletState;
    }

    public void setWalletState(String walletState) {
        this.walletState = walletState;
    }

    public String getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance = walletBalance;
    }

    public String getWalletMessage() {
        return walletMessage;
    }

    public void setWalletMessage(String walletMessage) {
        this.walletMessage = walletMessage;
    }
}
