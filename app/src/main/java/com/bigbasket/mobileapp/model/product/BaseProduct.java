package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BaseProduct implements Parcelable {
    public static final Parcelable.Creator<BaseProduct> CREATOR = new Parcelable.Creator<BaseProduct>() {
        @Override
        public BaseProduct createFromParcel(Parcel source) {
            return new BaseProduct(source);
        }

        @Override
        public BaseProduct[] newArray(int size) {
            return new BaseProduct[size];
        }
    };
    @SerializedName(Constants.PRODUCT_BRAND)
    private String brand;
    @SerializedName(Constants.PRODUCT_DESC)
    private String description;
    @SerializedName(Constants.IMAGE_URL)
    private String imageUrl;

    public BaseProduct(Parcel source) {
        brand = source.readString();
        description = source.readString();
        imageUrl = source.readString();
    }

    public BaseProduct(String brand, String description) {
        this.brand = brand;
        this.description = description;
    }

    public BaseProduct() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(brand);
        dest.writeString(description);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
