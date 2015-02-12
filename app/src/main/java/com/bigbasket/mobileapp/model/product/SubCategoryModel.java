package com.bigbasket.mobileapp.model.product;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class SubCategoryModel implements Parcelable, Serializable {

    @SerializedName(Constants.SLUG_NAME)
    private String slug;

    @SerializedName(Constants.DISPLAY_NAME)
    private String name;

    @SerializedName(Constants.SUB_CATS)
    private ArrayList<Category> category;

    public SubCategoryModel(Parcel source) {
        this.slug = source.readString();
        this.name = source.readString();
        boolean _wasCategoryNull = source.readByte() == (byte) 1;
        if (!_wasCategoryNull) {
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
        dest.writeString(this.name);
        boolean _wasCategoryNull = this.category == null;
        dest.writeByte(_wasCategoryNull ? (byte) 1 : (byte) 0);
        if (!_wasCategoryNull) {
            dest.writeTypedList(this.category);
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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
