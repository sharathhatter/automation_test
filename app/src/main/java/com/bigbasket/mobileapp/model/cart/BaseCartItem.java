package com.bigbasket.mobileapp.model.cart;

import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public abstract class BaseCartItem implements Parcelable {
    private double saving;
    private double mrp;

    @SerializedName(Constants.SALE_PRICE)
    private double salePrice;

    protected BaseCartItem() {
    }

    protected BaseCartItem(double saving, double mrp, double salePrice) {
        this.saving = saving;
        this.mrp = mrp;
        this.salePrice = salePrice;
    }

    public double getSaving() {
        return saving;
    }

    public double getMrp() {
        return mrp;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }
}
