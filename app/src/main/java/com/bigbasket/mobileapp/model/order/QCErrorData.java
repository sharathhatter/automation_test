package com.bigbasket.mobileapp.model.order;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class QCErrorData implements Parcelable {

    @SerializedName(Constants.QC_RESERVED_QUANTITY)
    private String reservedQuantity;

    @SerializedName(Constants.QC_ORIGINAL_QUANTITY)
    private String originalQuantity;

    @SerializedName(Constants.QC_RESERVED_PRODUCT)
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
