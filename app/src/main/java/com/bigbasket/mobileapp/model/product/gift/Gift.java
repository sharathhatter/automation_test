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
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    public Gift(Parcel source) {
        boolean isGiftNull = source.readByte() == (byte) 1;
        if (!isGiftNull) {
            this.giftItems = new ArrayList<>();
            source.readTypedList(giftItems, GiftItem.CREATOR);
        }
        this.commonMsg = source.readString();
        this.count = source.readInt();
        boolean isBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!isBaseImgUrlNull) {
            this.baseImgUrl = source.readString();
        }
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
        boolean isBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(isBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!isBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
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

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public int getCount() {
        return count;
    }
}
