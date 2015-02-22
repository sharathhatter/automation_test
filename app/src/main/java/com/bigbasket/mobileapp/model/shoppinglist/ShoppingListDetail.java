package com.bigbasket.mobileapp.model.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ShoppingListDetail implements Parcelable {

    @SerializedName(Constants.ITEMS)
    private ArrayList<Product> products;

    @SerializedName(Constants.TOP_CATEGORY_NAME)
    private String topCategoryName;

    @SerializedName(Constants.TOP_CAT_SLUG)
    private String topCategorySlug;

    @SerializedName(Constants.NUM_ITEMS)
    private int numItems;

    public ArrayList<Product> getProducts() {
        return products;
    }

    public String getTopCategoryName() {
        return topCategoryName;
    }

    public String getTopCategorySlug() {
        return topCategorySlug;
    }

    public static final Parcelable.Creator<ShoppingListDetail> CREATOR = new Parcelable.Creator<ShoppingListDetail>() {
        @Override
        public ShoppingListDetail createFromParcel(Parcel source) {
            return new ShoppingListDetail(source);
        }

        @Override
        public ShoppingListDetail[] newArray(int size) {
            return new ShoppingListDetail[size];
        }
    };

    public ShoppingListDetail(Parcel source) {
        this.products = new ArrayList<>();
        source.readTypedList(products, Product.CREATOR);
        this.topCategoryName = source.readString();
        this.topCategorySlug = source.readString();
        this.numItems = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(products);
        dest.writeString(topCategoryName);
        dest.writeString(topCategorySlug);
        dest.writeInt(numItems);
    }
}
