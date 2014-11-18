package com.bigbasket.mobileapp.model.product;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class SubCategoryModel implements Parcelable, Serializable {

    @SerializedName("slug_name")
    private String slug;

    @SerializedName("no_items")
    private int items;

    @SerializedName("display_name")
    private String name;

    @SerializedName("sub_cats")
    private ArrayList<Category> category;

    private boolean _wasCategoryNull;

    public SubCategoryModel(String slug, int items, String name, ArrayList<Category> category) {
        this.name = name;
        this.items = items;
        this.slug = slug;
        this.category = category;
    }

    public SubCategoryModel(Parcel source) {
        this.slug = source.readString();
        this.items = source.readInt();
        this.name = source.readString();
        this._wasCategoryNull = source.readByte() == (byte) 1;
        if (!this._wasCategoryNull) {
            source.readTypedList(category, Category.CREATOR);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.slug);
        dest.writeInt(this.items);
        dest.writeString(this.name);
        this._wasCategoryNull = this.category == null;
        dest.writeByte(this._wasCategoryNull ? (byte) 1 : (byte) 0);
        if (!this._wasCategoryNull) {
            dest.writeTypedList(this.category);
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Category> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<Category> category) {
        this.category = category;
    }

    public static final Parcelable.Creator<SubCategoryModel> CREATOR = new Parcelable.Creator<SubCategoryModel>() {
        @Override
        public SubCategoryModel createFromParcel(Parcel source) {
            return new SubCategoryModel(source);
        }

        @Override
        public SubCategoryModel[] newArray(int size) {
            return new SubCategoryModel[size];
        }
    };
}
