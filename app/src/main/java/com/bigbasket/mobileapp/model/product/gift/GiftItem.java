package com.bigbasket.mobileapp.model.product.gift;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GiftItem extends BaseGiftItem {
    public static final Parcelable.Creator<GiftItem> CREATOR = new Parcelable.Creator<GiftItem>() {
        @Override
        public GiftItem createFromParcel(Parcel source) {
            return new GiftItem(source);
        }

        @Override
        public GiftItem[] newArray(int size) {
            return new GiftItem[size];
        }
    };

    @SerializedName(Constants.MAX_NUM_CHARS)
    private int maxNumChars;
    @SerializedName(Constants.IS_READONLY)
    private boolean isReadOnly;

    public GiftItem(Parcel source) {
        super(source);
        this.maxNumChars = source.readInt();
        this.isReadOnly = source.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(maxNumChars);
        dest.writeByte(isReadOnly ? (byte) 1 : (byte) 0);
    }

    public int getMaxNumChars() {
        return maxNumChars;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
