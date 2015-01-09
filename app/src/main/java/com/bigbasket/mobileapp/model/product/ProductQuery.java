package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProductQuery implements Parcelable {

    private String type;
    private String slug;
    private String sortedOn;
    private boolean _wasSortedOnNull;
    private ArrayList<FilteredOn> filteredOn;
    private boolean _wasFilteredOnNull;
    private int page;


    public ProductQuery(String type, String slug, int page) {
        this.type = type;
        this.slug = slug;
        this.page = page;
    }

    public ProductQuery(String type, String slug) {
        this(type, slug, 1);
    }

    public ProductQuery(String type, String slug, String sortedOn, ArrayList<FilteredOn> filteredOn, int page) {
        this(type, slug, page);
        this.sortedOn = sortedOn;
        this.filteredOn = filteredOn;
    }

    public ProductQuery(Parcel source) {
        this.type = source.readString();
        this.slug = source.readString();
        this._wasSortedOnNull = source.readByte() == (byte) 1;
        if (!this._wasSortedOnNull) {
            this.sortedOn = source.readString();
        }
        this._wasFilteredOnNull = source.readByte() == (byte) 1;
        if (!this._wasFilteredOnNull) {
            String filteredOnStr = source.readString();
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Map<String, Set<String>>>() {
            }.getType();
            this.filteredOn = gson.fromJson(filteredOnStr, collectionType);
        }
        this.page = source.readInt();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSortedOn() {
        return sortedOn;
    }

    public void setSortedOn(String sortedOn) {
        this.sortedOn = sortedOn;
    }

    public ArrayList<FilteredOn> getFilteredOn() {
        return filteredOn;
    }

    public void setFilteredOn(ArrayList<FilteredOn> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Map<String, String> getAsQueryMap() {
        Map<String, String> productQueryMap = new HashMap<>();
        productQueryMap.put(Constants.TYPE, type);
        if (!TextUtils.isEmpty(slug)) {
            productQueryMap.put(Constants.SLUG, slug);
        }
        productQueryMap.put(Constants.CURRENT_PAGE, String.valueOf(page));
        if (!TextUtils.isEmpty(sortedOn)) {
            productQueryMap.put(Constants.SORT_ON, sortedOn);
        }
        if (filteredOn != null && !filteredOn.isEmpty()) {
            Gson gson = new Gson();
            productQueryMap.put(Constants.FILTER_ON,
                    gson.toJson(filteredOn));
        }
        return productQueryMap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.slug);
        this._wasSortedOnNull = this.sortedOn == null;
        dest.writeByte(this._wasSortedOnNull ? (byte) 1 : (byte) 0);
        if (!this._wasSortedOnNull) {
            dest.writeString(this.sortedOn);
        }
        this._wasFilteredOnNull = this.filteredOn == null;
        dest.writeByte(this._wasFilteredOnNull ? (byte) 1 : (byte) 0);
        if (!this._wasFilteredOnNull) {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Map<String, Set<String>>>() {
            }.getType();
            String filteredOnStr = gson.toJson(this.filteredOn, collectionType);
            dest.writeString(filteredOnStr);
        }
        dest.writeInt(this.page);
    }

    public static final Parcelable.Creator<ProductQuery> CREATOR = new Parcelable.Creator<ProductQuery>() {

        @Override
        public ProductQuery createFromParcel(Parcel source) {
            return new ProductQuery(source);
        }

        @Override
        public ProductQuery[] newArray(int size) {
            return new ProductQuery[size];
        }
    };
}
