package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
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
    private ArrayList<FilteredOn> filteredOn;
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
        boolean _wasSortedOnNull = source.readByte() == (byte) 1;
        if (!_wasSortedOnNull) {
            this.sortedOn = source.readString();
        }
        boolean _wasFilteredOnNull = source.readByte() == (byte) 1;
        if (!_wasFilteredOnNull) {
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
        boolean _wasSortedOnNull = this.sortedOn == null;
        dest.writeByte(_wasSortedOnNull ? (byte) 1 : (byte) 0);
        if (!_wasSortedOnNull) {
            dest.writeString(this.sortedOn);
        }
        boolean _wasFilteredOnNull = this.filteredOn == null;
        dest.writeByte(_wasFilteredOnNull ? (byte) 1 : (byte) 0);
        if (!_wasFilteredOnNull) {
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

    @Nullable
    public static ProductQuery convertDestinationTypeToProductQuery(String destinationType,
                                                                    String destinationSlug) {
        switch (destinationType) {
            case DestinationInfo.PRODUCT_CATEGORY:
                return new ProductQuery(ProductListType.CATEGORY.get(), destinationSlug, 1);
            case DestinationInfo.SEARCH:
                return new ProductQuery(ProductListType.SEARCH.get(), destinationSlug, 1);
            case DestinationInfo.PRODUCT_LIST:
                if (!TextUtils.isEmpty(destinationSlug)) {
                    String[] destinationInfo = destinationSlug.split("&");
                    if (destinationInfo.length == 1) {
                        return new ProductQuery(destinationSlug, null, 1);
                    } else if (destinationInfo.length == 2) {
                        return new ProductQuery(destinationInfo[0], destinationInfo[1], 1);
                    }
                }
                break;
        }
        return null;
    }
}
