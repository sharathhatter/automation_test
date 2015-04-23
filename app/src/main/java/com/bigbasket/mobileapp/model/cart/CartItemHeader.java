package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItemHeader implements Parcelable {

    public static final Parcelable.Creator<CartItemHeader> CREATOR = new Parcelable.Creator<CartItemHeader>() {
        @Override
        public CartItemHeader createFromParcel(Parcel source) {
            return new CartItemHeader(source);
        }

        @Override
        public CartItemHeader[] newArray(int size) {
            return new CartItemHeader[size];
        }
    };
    private String topCatName;
    private double topCatTotal;
    private int topCatItems;
    private CartItem cartItems;

    public CartItemHeader() {
    }

    public CartItemHeader(Parcel source) {
        topCatName = source.readString();
        topCatTotal = source.readDouble();
        topCatItems = source.readInt();
        cartItems = source.readParcelable(CartItemHeader.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topCatName);
        dest.writeDouble(topCatTotal);
        dest.writeInt(topCatItems);
        dest.writeParcelable(cartItems, flags);
    }

    public String getTopCatName() {
        return topCatName;
    }

    public void setTopCatName(String topCatName) {
        this.topCatName = topCatName;
    }

    public double getTopCatTotal() {
        return topCatTotal;
    }

    public void setTopCatTotal(double topCatTotal) {
        this.topCatTotal = topCatTotal;
    }

    public int getTopCatItems() {
        return topCatItems;
    }

    public void setTopCatItems(int topCatItems) {
        this.topCatItems = topCatItems;
    }
}
