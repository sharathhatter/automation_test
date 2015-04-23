package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartItemPromoInfo implements Parcelable {

    public static final Parcelable.Creator<CartItemPromoInfo> CREATOR = new Parcelable.Creator<CartItemPromoInfo>() {
        @Override
        public CartItemPromoInfo createFromParcel(Parcel source) {
            return new CartItemPromoInfo(source);
        }

        @Override
        public CartItemPromoInfo[] newArray(int size) {
            return new CartItemPromoInfo[size];
        }
    };
    @SerializedName(Constants.PROMO)
    private CartItemPromo promoInfo;
    @SerializedName(Constants.REGULAR)
    private CartItemPromo regularInfo;

    CartItemPromoInfo(Parcel source) {
        boolean isPromoInfoNull = source.readByte() == (byte) 1;
        if (!isPromoInfoNull) {
            promoInfo = source.readParcelable(CartItemPromoInfo.class.getClassLoader());
        }
        boolean isRegularInfoNull = source.readByte() == (byte) 1;
        if (!isRegularInfoNull) {
            regularInfo = source.readParcelable(CartItemPromoInfo.class.getClassLoader());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isPromoInfoNull = promoInfo == null;
        dest.writeByte(isPromoInfoNull ? (byte) 1 : (byte) 0);
        if (!isPromoInfoNull) {
            dest.writeParcelable(promoInfo, flags);
        }
        boolean isRegularInfoNull = regularInfo == null;
        dest.writeByte(isRegularInfoNull ? (byte) 1 : (byte) 0);
        if (!isRegularInfoNull) {
            dest.writeParcelable(regularInfo, flags);
        }
    }

    public CartItemPromo getPromoInfo() {
        return promoInfo;
    }

    public CartItemPromo getRegularInfo() {
        return regularInfo;
    }
}
