package com.bigbasket.mobileapp.model.product;


import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Parcelable, Serializable {

    @SerializedName("slug_name")
    private String slug;

    @SerializedName("sub_cats")
    private ArrayList<Category> category;

    @SerializedName("no_items")
    private int numberItems;

    @SerializedName("display_name")
    private String name;


    public Category(String slug, ArrayList<Category> category, int numberItems, String name) {
        this.slug = slug;
        this.category = category;
        this.numberItems = numberItems;
        this.name = name;
    }

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Category(Parcel source) {
        this.slug = source.readString();
        this.numberItems = source.readInt();
        this.name = source.readString();
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

    public int getNumberItems() {
        return numberItems;
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
        dest.writeInt(this.numberItems);
        dest.writeString(this.name);
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
}
