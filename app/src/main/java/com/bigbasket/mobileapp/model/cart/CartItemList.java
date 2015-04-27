package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartItemList implements Parcelable {

    public static final Parcelable.Creator<CartItemList> CREATOR = new Parcelable.Creator<CartItemList>() {
        @Override
        public CartItemList createFromParcel(Parcel source) {
            return new CartItemList(source);
        }

        @Override
        public CartItemList[] newArray(int size) {
            return new CartItemList[size];
        }
    };
    @SerializedName(Constants.LINE_ITEMS)
    private ArrayList<CartItem> cartItems;
    @SerializedName(Constants.TLC_NAME)
    private String topCatName;
    @SerializedName(Constants.TLC_TOTAL)
    private double topCatTotal;
    @SerializedName(Constants.TLC_NUM_ITEMS)
    private int topCatItems;

    public CartItemList() {
        cartItems = new ArrayList<>();
    }

    public CartItemList(ArrayList<CartItem> cartItems, String topCatName, double topCatTotal, int topCatItems) {
        this.cartItems = cartItems;
        this.topCatName = topCatName;
        this.topCatTotal = topCatTotal;
        this.topCatItems = topCatItems;
    }

    CartItemList(Parcel source) {
        cartItems = new ArrayList<>();
        source.readTypedList(cartItems, CartItem.CREATOR);
        topCatName = source.readString();
        topCatTotal = source.readDouble();
        topCatItems = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(cartItems);
        dest.writeString(topCatName);
        dest.writeDouble(topCatTotal);
        dest.writeInt(topCatItems);
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public String getTopCatName() {
        return topCatName;
    }

    public double getTopCatTotal() {
        return topCatTotal;
    }

    public int getTopCatItems() {
        return topCatItems;
    }
}
