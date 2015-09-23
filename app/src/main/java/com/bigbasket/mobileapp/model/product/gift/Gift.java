package com.bigbasket.mobileapp.model.product.gift;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Gift implements Parcelable {
    public static final Parcelable.Creator<Gift> CREATOR = new Parcelable.Creator<Gift>() {
        @Override
        public Gift createFromParcel(Parcel source) {
            return new Gift(source);
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };

    @SerializedName(Constants.ITEMS)
    private ArrayList<GiftItem> giftItems;
    @SerializedName(Constants.COMMON_MSG)
    private String commonMsg;
    private int count;

    public Gift(Parcel source) {
        boolean isGiftNull = source.readByte() == (byte) 1;
        if (!isGiftNull) {
            this.giftItems = new ArrayList<>();
            source.readTypedList(giftItems, GiftItem.CREATOR);
        }
        this.commonMsg = source.readString();
        this.count = source.readInt();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isGiftNull = giftItems == null;
        dest.writeByte(isGiftNull ? (byte) 1 : (byte) 0);
        if (!isGiftNull) {
            dest.writeTypedList(giftItems);
        }
        dest.writeString(commonMsg);
        dest.writeInt(count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<GiftItem> getGiftItems() {
        return giftItems;
    }

    public String getCommonMsg() {
        return commonMsg;
    }

    public int getCount() {
        return count;
    }
}
