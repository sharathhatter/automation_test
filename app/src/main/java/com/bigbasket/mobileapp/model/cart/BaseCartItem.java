package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BaseCartItem implements Parcelable {
    public static final Parcelable.Creator<BaseCartItem> CREATOR = new Parcelable.Creator<BaseCartItem>() {
        @Override
        public BaseCartItem createFromParcel(Parcel source) {
            return new BaseCartItem(source);
        }

        @Override
        public BaseCartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };
    private double saving;
    private double mrp;
    @SerializedName(Constants.SALE_PRICE)
    private double salePrice;

    protected BaseCartItem() {
    }

    BaseCartItem(Parcel source) {
        saving = source.readDouble();
        mrp = source.readDouble();
        salePrice = source.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(saving);
        dest.writeDouble(mrp);
        dest.writeDouble(salePrice);
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }
}
