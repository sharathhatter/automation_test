package com.bigbasket.mobileapp.model.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ShopInShop implements Parcelable {

    private String name;
    private String slug;

    @SerializedName(Constants.IS_EXPRESS)
    private boolean isExpress;

    @SerializedName(Constants.IS_DISCOUNT)
    private boolean isDiscount;

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public boolean getIsExpress() {
        return isExpress;
    }

    public boolean getIsDiscount() {
        return isDiscount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(slug);
        dest.writeByte(isExpress ? (byte) 1 : (byte) 0);
        dest.writeByte(isDiscount ? (byte) 1 : (byte) 0);
    }

    public ShopInShop(Parcel source) {
        name = source.readString();
        slug = source.readString();
        isExpress = source.readByte() == (byte) 1;
        isDiscount = source.readByte() == (byte) 1;
    }

    public static final Parcelable.Creator<ShopInShop> CREATOR = new Parcelable.Creator<ShopInShop>() {
        @Override
        public ShopInShop createFromParcel(Parcel source) {
            return new ShopInShop(source);
        }

        @Override
        public ShopInShop[] newArray(int size) {
            return new ShopInShop[size];
        }
    };
}
