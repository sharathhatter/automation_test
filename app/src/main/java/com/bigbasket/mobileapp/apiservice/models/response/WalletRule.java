package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 3/3/15.
 */
public class WalletRule implements Parcelable{

    @SerializedName(Constants.VOUCHER_PER_RULE)
    public int voucherPerRule;

    @SerializedName(Constants.AMOUNT_PER_VOUCHER)
    public float amountPerVoucher;

    @SerializedName(Constants.TC)
    public String termAndCondition;

    @SerializedName(Constants.AVAILABLE_DELIVERY_TOKEN)
    public int availableDeliveryToken;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(voucherPerRule);
        dest.writeFloat(amountPerVoucher);
        dest.writeString(termAndCondition);
        dest.writeInt(availableDeliveryToken);
    }

    public WalletRule(Parcel source) {
        voucherPerRule = source.readInt();
        amountPerVoucher = source.readFloat();
        termAndCondition = source.readString();
        availableDeliveryToken = source.readInt();
    }

    public static final Parcelable.Creator<WalletRule> CREATOR = new Parcelable.Creator<WalletRule>() {
        @Override
        public WalletRule createFromParcel(Parcel source) {
            return new WalletRule(source);
        }

        @Override
        public WalletRule[] newArray(int size) {
            return new WalletRule[size];
        }
    };
}
