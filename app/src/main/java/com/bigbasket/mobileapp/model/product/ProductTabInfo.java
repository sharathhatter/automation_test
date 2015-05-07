package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ProductTabInfo implements Parcelable {
    @SerializedName(Constants.TAB_NAME)
    private String tabName;

    @SerializedName(Constants.TAB_TYPE)
    private String tabType;

    @SerializedName(Constants.PRODUCT_INFO)
    private ProductInfo productInfo;

    public ProductTabInfo(Parcel source) {
        tabName = source.readString();
        tabType = source.readString();

        boolean isProductListInfoNull = source.readByte() == (byte) 1;
        if (!isProductListInfoNull) {
            productInfo = source.readParcelable(ProductTabInfo.class.getClassLoader());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tabName);
        dest.writeString(tabType);

        boolean isProductListDataNull = productInfo == null;
        dest.writeByte(isProductListDataNull ? (byte) 1 : (byte) 0);
        if (!isProductListDataNull) {
            dest.writeParcelable(productInfo, flags);
        }
    }

    public static final Parcelable.Creator<ProductTabInfo> CREATOR = new Parcelable.Creator<ProductTabInfo>() {
        @Override
        public ProductTabInfo createFromParcel(Parcel source) {
            return new ProductTabInfo(source);
        }

        @Override
        public ProductTabInfo[] newArray(int size) {
            return new ProductTabInfo[size];
        }
    };

    public String getTabName() {
        return tabName;
    }

    public String getTabType() {
        return tabType;
    }

    @Nullable
    public ProductInfo getProductInfo() {
        return productInfo;
    }
}
