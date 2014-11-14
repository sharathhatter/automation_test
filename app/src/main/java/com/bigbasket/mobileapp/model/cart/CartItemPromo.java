package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartItemPromo extends BaseCartItem {

    @SerializedName(Constants.PROMO_ID)
    private int promoId;

    @SerializedName(Constants.PROMO_NAME)
    private String promoName;

    @SerializedName(Constants.NO_ITEM_IN_CART)
    private double numItemInCart;

    @SerializedName(Constants.PROMO_LABEL)
    protected String promo_label;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(promoId);
        dest.writeString(promoName);
        dest.writeDouble(numItemInCart);
        dest.writeString(promo_label);
        dest.writeDouble(getMrp());
        dest.writeDouble(getSalePrice());
        dest.writeDouble(getSaving());
    }

    CartItemPromo(Parcel source) {
        promoId = source.readInt();
        promoName = source.readString();
        numItemInCart = source.readDouble();
        promo_label = source.readString();
        setMrp(source.readDouble());
        setSalePrice(source.readDouble());
        setSaving(source.readDouble());
    }

    public static final Parcelable.Creator<CartItemPromo> CREATOR = new Parcelable.Creator<CartItemPromo>() {
        @Override
        public CartItemPromo createFromParcel(Parcel source) {
            return new CartItemPromo(source);
        }

        @Override
        public CartItemPromo[] newArray(int size) {
            return new CartItemPromo[size];
        }
    };

    public int getPromoId() {
        return promoId;
    }

    public String getPromoName() {
        return promoName;
    }

    public double getNumItemInCart() {
        return numItemInCart;
    }
}
