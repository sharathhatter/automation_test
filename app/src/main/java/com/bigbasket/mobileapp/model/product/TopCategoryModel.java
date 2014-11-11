package com.bigbasket.mobileapp.model.product;


import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TopCategoryModel implements Parcelable, Serializable {

    @SerializedName(Constants.TOP_CATEGORY)
    private String name;

    private String slug;
    private String version;

    @SerializedName(Constants.ICON)
    private String imagePath;


    @SerializedName(Constants.FLAT_PAGE)
    private String flatPage;


    public TopCategoryModel() {

    }

    public TopCategoryModel(Cursor cursor) {
        this.slug = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_SLUG));
        this.version = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_VERSION));
        this.name = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_NAME));
        this.imagePath = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_IMAGE_PATH));
        this.flatPage = cursor.getString(cursor.getColumnIndex(CategoryAdapter.COLUMN_FLAT_PAGE));

    }

    public TopCategoryModel(String name, String slug, String version, String imagePath, String flatPage) {
        this.name = name;
        this.slug = slug;
        this.version = version;
        this.imagePath = imagePath;
        this.flatPage = flatPage;
    }

    public TopCategoryModel(Parcel source) {
        this.name = source.readString();
        this.slug = source.readString();
        this.version = source.readString();
        this.imagePath = source.readString();
    }

//    public TopCategoryModel(String name, String slug, String version, String imagePath) {
//        this.name = name;
//        this.slug = slug;
//        this.version = version;
//        this.imagePath = imagePath;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.slug);
        dest.writeString(this.version);
        dest.writeString(this.imagePath);
    }

    public static final Parcelable.Creator<TopCategoryModel> CREATOR = new Parcelable.Creator<TopCategoryModel>() {

        @Override
        public TopCategoryModel createFromParcel(Parcel source) {
            return new TopCategoryModel(source);
        }

        @Override
        public TopCategoryModel[] newArray(int size) {
            return new TopCategoryModel[size];
        }
    };

    public String getFlatPage() {
        return flatPage;
    }

    public void setFlatPage(String flatPage) {
        this.flatPage = flatPage;
    }
}
