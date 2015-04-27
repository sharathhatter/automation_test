package com.bigbasket.mobileapp.model.cart;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartSummary implements Parcelable {

    public static final Parcelable.Creator<CartSummary> CREATOR = new Parcelable.Creator<CartSummary>() {
        @Override
        public CartSummary createFromParcel(Parcel source) {
            return new CartSummary(source);
        }

        @Override
        public CartSummary[] newArray(int size) {
            return new CartSummary[size];
        }
    };
    @SerializedName(Constants.TOTAL)
    private double total;
    @SerializedName(Constants.SAVINGS)
    private double savings;
    @SerializedName(Constants.NUM_ITEMS)
    private int noOfItems;

    public CartSummary(Parcel source) {
        total = source.readDouble();
        savings = source.readDouble();
        noOfItems = source.readInt();
    }

    public CartSummary() {
    }

    public CartSummary(double total, double savings, int noOfItems) {
        this.total = total;
        this.savings = savings;
        this.noOfItems = noOfItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(total);
        dest.writeDouble(savings);
        dest.writeInt(noOfItems);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }

    public int getNoOfItems() {
        return noOfItems;
    }

    public void setNoOfItems(int noOfItems) {
        this.noOfItems = noOfItems;
    }

}
