package com.bigbasket.mobileapp.model.product;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Parcelable, Serializable {

    @SerializedName(Constants.SLUG_NAME)
    private String slug;

    @SerializedName(Constants.SUB_CATS)
    private ArrayList<Category> category;

    @SerializedName(Constants.DISPLAY_NAME)
    private String name;

    @SerializedName(Constants.FILTER)
    private String filter;

    @SerializedName(Constants.SORT_BY)
    private String sortBy;

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Category(Parcel source) {
        this.slug = source.readString();
        this.name = source.readString();
        boolean _wasFilterNull = source.readByte() == (byte) 1;
        if (!_wasFilterNull) {
            filter = source.readString();
        }

        boolean _wasSortByrNull = source.readByte() == (byte) 1;
        if (!_wasSortByrNull) {
            sortBy = source.readString();
        }
        boolean _wasCategoriesNull = source.readByte() == (byte) 1;
        if (!_wasCategoriesNull) {
            source.readTypedList(this.category, Category.CREATOR);
        }
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

    public ArrayList<Category> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<Category> category) {
        this.category = category;
    }

    public void setCategory(JsonArray jsonCategoryArray) {
        ArrayList<Category> categories = null;
        if (jsonCategoryArray != null && jsonCategoryArray.size() > 0) {
            categories = new ArrayList<>();
            for (JsonElement catJsonElement : jsonCategoryArray) {
                JsonObject catJsonObject = catJsonElement.getAsJsonObject();
                JsonArray catJsonArray = catJsonObject.get("cat").getAsJsonArray();
                if (catJsonArray != null && catJsonArray.size() > 0) {
                    Category category = new Category(catJsonArray.get(0).getAsString(), catJsonArray.get(1).getAsString());
                    categories.add(category);
                }
            }
        }
        this.category = categories;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.slug);
        dest.writeString(this.name);
        boolean _wasFilterNull = filter == null;
        dest.writeByte(_wasFilterNull ? (byte) 1 : (byte) 0);
        if (!_wasFilterNull) {
            dest.writeString(filter);
        }
        boolean _wasSortByNull = filter == null;
        dest.writeByte(_wasSortByNull ? (byte) 1 : (byte) 0);
        if (!_wasSortByNull) {
            dest.writeString(sortBy);
        }
        boolean _wasCategoriesNull = category == null;
        dest.writeByte(_wasCategoriesNull ? (byte) 1 : (byte) 0);
        if (!_wasCategoriesNull) {
            dest.writeTypedList(category);
        }
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getFilter() {
        return filter;
    }

    public String getSortBy() {
        return sortBy;
    }
}
