package com.bigbasket.mobileapp.model.shoppinglist;


import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ShoppingListName implements Parcelable {

    @SerializedName(Constants.SHOPPING_LIST_NAME)
    private String name;

    @SerializedName(Constants.SHOPPING_LIST_SLUG)
    private String slug;

    @SerializedName(Constants.SHOPPING_LIST_IS_SYSTEM)
    private boolean isSystem;

    public ShoppingListName() {
    }

    public ShoppingListName(String name, String slug, boolean system) {
        this.name = name;
        this.slug = slug;
        isSystem = system;
    }

    public ShoppingListName(Parcel source) {
        this.name = source.readString();
        this.slug = source.readString();
        this.isSystem = source.readByte() == (byte) 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.slug);
        dest.writeByte(this.isSystem ? (byte) 1 : (byte) 0);
    }

    public static final Parcelable.Creator<ShoppingListName> CREATOR = new Parcelable.Creator<ShoppingListName>() {
        @Override
        public ShoppingListName createFromParcel(Parcel source) {
            return new ShoppingListName(source);
        }

        @Override
        public ShoppingListName[] newArray(int size) {
            return new ShoppingListName[size];
        }
    };
}
