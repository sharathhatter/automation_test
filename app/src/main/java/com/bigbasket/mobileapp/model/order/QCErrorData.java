package com.bigbasket.mobileapp.model.order;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.product.Product;

public class QCErrorData implements Parcelable {

    private String reservedQuantity;
    private String originalQuantity;
    private Product product;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reservedQuantity);
        dest.writeString(originalQuantity);
        dest.writeParcelable(product, flags);
    }

    public QCErrorData(Parcel source) {
        reservedQuantity = source.readString();
        originalQuantity = source.readString();
        product = source.readParcelable(QCErrorData.class.getClassLoader());
    }

    public static final Parcelable.Creator<QCErrorData> CREATOR = new Parcelable.Creator<QCErrorData>() {
        @Override
        public QCErrorData createFromParcel(Parcel source) {
            return new QCErrorData(source);
        }

        @Override
        public QCErrorData[] newArray(int size) {
            return new QCErrorData[size];
        }
    };

    public QCErrorData(String reservedQuantity, String originalQuantity, Product product) {
        this.reservedQuantity = reservedQuantity;
        this.originalQuantity = originalQuantity;
        this.product = product;
    }

    public String getReservedQuantity() {
        return reservedQuantity;
    }

    public String getOriginalQuantity() {
        return originalQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
