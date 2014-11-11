package com.bigbasket.mobileapp.model.cart;


import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartSummary implements Parcelable {

    @SerializedName(Constants.TOTAL)
    private double total;

    @SerializedName(Constants.SAVINGS)
    private double savings;

    @SerializedName(Constants.NUM_ITEMS)
    private int noOfItems;

    @SerializedName(Constants.IS_NOT_SUPPORTED)
    private boolean isNotSupported;

//    @SerializedName(Constants.IS_FORCE_UPDATE)
//    private boolean isForceUpdate;

    @SerializedName(Constants.ENABLE_KONOTOR)
    private boolean isKonotorEnabled;

    @SerializedName(Constants.APP_EXPIRE_BY)
    private String appExpireBy;

    @SerializedName(Constants.IS_UPDATE_REQUIRED)
    private int isUpdateRequired;

    @SerializedName(Constants.HAS_EXPRESS)
    private boolean hasExpressShops;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(total);
        dest.writeDouble(savings);
        dest.writeInt(noOfItems);
        dest.writeByte(isNotSupported ? (byte) 1 : (byte) 0);
        dest.writeByte(isKonotorEnabled ? (byte) 1 : (byte) 0);
        boolean isAppExpiredByNull = appExpireBy == null;
        dest.writeByte(isAppExpiredByNull ? (byte) 1 : (byte) 0);
        if (!isAppExpiredByNull) {
            dest.writeString(appExpireBy);
        }
        dest.writeInt(isUpdateRequired);
        dest.writeByte(hasExpressShops ? (byte) 1 : (byte) 0);
    }

    public CartSummary(Parcel source) {
        total = source.readDouble();
        savings = source.readDouble();
        noOfItems = source.readInt();
        isNotSupported = source.readByte() == (byte) 1;
        isKonotorEnabled = source.readByte() == (byte) 1;
        boolean isAppExpiredByNull = source.readByte() == (byte) 1;
        if (!isAppExpiredByNull) {
            appExpireBy = source.readString();
        }
        isUpdateRequired = source.readInt();
        hasExpressShops = source.readByte() == (byte) 1;
    }

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

    public CartSummary() {
    }

    public CartSummary(double total, double savings, int noOfItems) {
        this.total = total;
        this.savings = savings;
        this.noOfItems = noOfItems;
    }

    public CartSummary(double total, double savings, int noOfItems, boolean isNotSupported,
                       boolean isKonotorEnabled, String appExpireBy, boolean hasExpressShops) {
        this(total, savings, noOfItems);
        this.isNotSupported = isNotSupported;
        this.isKonotorEnabled = isKonotorEnabled;
        this.appExpireBy = appExpireBy;
        this.hasExpressShops = hasExpressShops;
    }

    public boolean hasExpressShops() {
        return hasExpressShops;
    }

    public void setHasExpressShops(boolean hasExpressShops) {
        this.hasExpressShops = hasExpressShops;
    }

    public int getIsUpdateRequired() {
        return isUpdateRequired;
    }

    public void setIsUpdateRequired(int isUpdateRequired) {
        this.isUpdateRequired = isUpdateRequired;
    }

    public String getAppExpireBy() {
        return appExpireBy;
    }

    public void setAppExpireBy(String appExpireBy) {
        this.appExpireBy = appExpireBy;
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

    public boolean isNotSupported() {
        return isNotSupported;
    }

    public boolean isKonotorEnabled() {
        return isKonotorEnabled;
    }

    public void setNotSupported(boolean isNotSupported) {
        this.isNotSupported = isNotSupported;
    }

    public void setKonotorEnabled(boolean isKonotorEnabled) {
        this.isKonotorEnabled = isKonotorEnabled;
    }
}
