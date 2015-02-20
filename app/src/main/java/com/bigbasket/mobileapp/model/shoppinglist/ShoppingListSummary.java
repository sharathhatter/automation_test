package com.bigbasket.mobileapp.model.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ShoppingListSummary implements Parcelable {

    @SerializedName(Constants.TOP_CATEGORY_NAME)
    private String topCategoryName;

    @SerializedName(Constants.TOP_CAT_SLUG)
    private String topCategorySlug;

    @SerializedName(Constants.CART_INFO_NUM_OF_ITEMS)
    private String numItems;

    public ShoppingListSummary(String topCategoryName, String topCategorySlug, String numItems) {
        this.topCategoryName = topCategoryName;
        this.topCategorySlug = topCategorySlug;
        this.numItems = numItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topCategoryName);
        dest.writeString(topCategorySlug);
        dest.writeString(numItems);
    }

    public ShoppingListSummary(Parcel source) {
        topCategoryName = source.readString();
        topCategorySlug = source.readString();
        numItems = source.readString();
    }

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

    public String getTopCategoryName() {
        return topCategoryName;
    }

    public String getTopCategorySlug() {
        return topCategorySlug;
    }

    public String getNumItems() {
        return numItems;
    }

    @Nullable
    public String getNumItemsDisplay() {
        if (!TextUtils.isEmpty(numItems) && TextUtils.isDigitsOnly(numItems)) {
            if (Integer.parseInt(numItems) > 1) {
                return numItems + " ITEMS";
            }
            return numItems + " ITEM";
        }
        return null;
    }
}
