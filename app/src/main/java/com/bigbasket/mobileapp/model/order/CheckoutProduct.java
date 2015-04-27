package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckoutProduct implements Parcelable {

    public static final Parcelable.Creator<CheckoutProduct> CREATOR = new Parcelable.Creator<CheckoutProduct>() {
        @Override
        public CheckoutProduct createFromParcel(Parcel source) {
            return new CheckoutProduct(source);
        }

        @Override
        public CheckoutProduct[] newArray(int size) {
            return new CheckoutProduct[size];
        }
    };
    private String reserveQuantity;
    private String originalQuantity;
    private String description;
    private String spprice;
    private String mrp;
    private String brand;
    private String discountValue;
    private String id;
    private String weight;
    private String categoryName;
    private String topLevelCategorySlug;

    public CheckoutProduct(Parcel source) {
        reserveQuantity = source.readString();
        originalQuantity = source.readString();
        description = source.readString();
        spprice = source.readString();
        mrp = source.readString();
        brand = source.readString();
        discountValue = source.readString();
        id = source.readString();
        weight = source.readString();
        categoryName = source.readString();
        topLevelCategorySlug = source.readString();
    }

    public CheckoutProduct() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reserveQuantity);
        dest.writeString(originalQuantity);
        dest.writeString(description);
        dest.writeString(spprice);
        dest.writeString(mrp);
        dest.writeString(brand);
        dest.writeString(discountValue);
        dest.writeString(id);
        dest.writeString(weight);
        dest.writeString(categoryName);
        dest.writeString(topLevelCategorySlug);
    }

    public String getReserveQuantity() {
        return reserveQuantity;
    }

    public void setReserveQuantity(String reservequantity) {
        this.reserveQuantity = reservequantity;
    }

    public String getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(String originalquantity) {
        this.originalQuantity = originalquantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String productdescription) {
        this.description = productdescription;
    }

    public String getSpprice() {
        return spprice;
    }

    public void setSpprice(String spprice) {
        this.spprice = spprice;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }


    public String getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(String discountvalue) {
        this.discountValue = discountvalue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String catgeryname) {
        this.categoryName = catgeryname;
    }

    public String getTopLevelCategorySlug() {
        return topLevelCategorySlug;
    }

    public void setTopLevelCategorySlug(String topLevelCategorySlug) {
        this.topLevelCategorySlug = topLevelCategorySlug;
    }
}
