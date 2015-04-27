package com.bigbasket.mobileapp.model.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ShoppingListSummary implements Parcelable {

    public static final Parcelable.Creator<ShoppingListSummary> CREATOR = new Parcelable.Creator<ShoppingListSummary>() {
        @Override
        public ShoppingListSummary createFromParcel(Parcel source) {
            return new ShoppingListSummary(source);
        }

        @Override
        public ShoppingListSummary[] newArray(int size) {
            return new ShoppingListSummary[size];
        }
    };
    @SerializedName(Constants.FACET_NAME)
    private String facetName;
    private ArrayList<Product> products;
    @SerializedName(Constants.PRODUCT_COUNT)
    private int numProducts;
    @SerializedName(Constants.FACET_SLUG)
    private String facetSlug;
    private ShoppingListName shoppingListName;

    public ShoppingListSummary(Parcel source) {
        facetName = source.readString();
        products = new ArrayList<>();
        source.readTypedList(products, Product.CREATOR);
        numProducts = source.readInt();
        facetSlug = source.readString();
        shoppingListName = source.readParcelable(ShoppingListSummary.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(facetName);
        dest.writeTypedList(products);
        dest.writeInt(numProducts);
        dest.writeString(facetSlug);
        dest.writeParcelable(shoppingListName, flags);
    }

    public String getFacetName() {
        return facetName;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public int getNumProducts() {
        return numProducts;
    }

    public String getFacetSlug() {
        return facetSlug;
    }

    public ShoppingListName getShoppingListName() {
        return shoppingListName;
    }

    public void setShoppingListName(ShoppingListName shoppingListName) {
        this.shoppingListName = shoppingListName;
    }

    public String getTitle() {
        return facetName + " (" + numProducts + ")";
    }
}
