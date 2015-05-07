package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductInfo implements Parcelable {

    public static final Parcelable.Creator<ProductInfo> CREATOR = new Parcelable.Creator<ProductInfo>() {
        @Override
        public ProductInfo createFromParcel(Parcel source) {
            return new ProductInfo(source);
        }

        @Override
        public ProductInfo[] newArray(int size) {
            return new ProductInfo[size];
        }
    };

    @SerializedName(Constants.PRODUCT_COUNT)
    private int productCount;
    @SerializedName(Constants.CURRENT_PAGE)
    private int currentPage;
    @SerializedName(Constants.TOTAL_PAGES)
    private int totalPages;
    @SerializedName(Constants.PRODUCTS)
    private ArrayList<Product> products;

    public ProductInfo(Parcel source) {
        productCount = source.readInt();
        currentPage = source.readInt();
        totalPages = source.readInt();

        boolean isProductsNull = source.readByte() == (byte) 1;
        if (!isProductsNull) {
            products = new ArrayList<>();
            source.readTypedList(products, Product.CREATOR);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(productCount);
        dest.writeInt(currentPage);
        dest.writeInt(totalPages);

        boolean isProductsNull = products == null;
        dest.writeByte(isProductsNull ? (byte) 1 : (byte) 0);
        if (!isProductsNull) {
            dest.writeTypedList(products);
        }

    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public int getProductCount() {
        return productCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @Nullable
    public ArrayList<Product> getProducts() {
        return products;
    }
}
