package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductListData implements Parcelable {

    @SerializedName(Constants.SORT_ON)
    private String sortedOn;

    @SerializedName(Constants.PRODUCT_COUNT)
    private int productCount;

    @SerializedName(Constants.CURRENT_PAGE)
    private int currentPage;

    @SerializedName(Constants.TOTAL_PAGES)
    private int totalPages;

    @SerializedName(Constants.SEARCH_QUERY)
    private String query;

    @SerializedName(Constants.PRODUCTS)
    private List<Product> products;

    @SerializedName(Constants.FILTERED_ON)
    private ArrayList<FilteredOn> filteredOn;

    @SerializedName(Constants.PRODUCT_SORT_OPTION)
    private ArrayList<Option> sortOptions;

    @SerializedName(Constants.FILTER_OPTIONS)
    private ArrayList<FilterOptionCategory> filterOptionItems;

    @Override
    public int describeContents() {
        return 0;
    }

    public ProductListData() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isSortedOnNull = sortedOn == null;
        dest.writeByte(isSortedOnNull ? (byte) 1 : (byte) 0);
        if (!isSortedOnNull) {
            dest.writeString(sortedOn);
        }
        dest.writeInt(productCount);
        dest.writeInt(currentPage);
        dest.writeInt(totalPages);
        boolean isQueryNull = query == null;
        dest.writeByte(isQueryNull ? (byte) 1 : (byte) 0);
        if (!isQueryNull) {
            query = dest.readString();
        }
        boolean isProductsNull = products == null;
        dest.writeByte(isProductsNull ? (byte) 1 : (byte) 0);
        if (!isProductsNull) {
            dest.writeTypedList(products);
        }
        boolean isFilteredOnNull = filteredOn == null;
        dest.writeByte(isFilteredOnNull ? (byte) 1 : (byte) 0);
        if (!isFilteredOnNull) {
            dest.writeTypedList(filteredOn);
        }
        boolean isSortOptionsNull = sortOptions == null;
        dest.writeByte(isSortOptionsNull ? (byte) 1 : (byte) 0);
        if (!isSortOptionsNull) {
            dest.writeTypedList(sortOptions);
        }
        boolean isFilterOptionCategoryNull = filterOptionItems == null;
        dest.writeByte(isFilterOptionCategoryNull ? (byte) 1 : (byte) 0);
        if (!isFilterOptionCategoryNull) {
            dest.writeTypedList(filterOptionItems);
        }
    }

    public ProductListData(Parcel source) {
        boolean isSortedOnNull = source.readByte() == (byte) 1;
        if (!isSortedOnNull) {
            sortedOn = source.readString();
        }
        productCount = source.readInt();
        currentPage = source.readInt();
        totalPages = source.readInt();
        boolean isQueryNull = source.readByte() == (byte) 1;
        if (!isQueryNull) {
            query = source.readString();
        }
        boolean isProductsNull = source.readByte() == (byte) 1;
        if (!isProductsNull) {
            products = new ArrayList<>();
            source.readTypedList(products, Product.CREATOR);
        }
        boolean isFilteredOnNull = source.readByte() == (byte) 1;
        if (!isFilteredOnNull) {
            filteredOn = new ArrayList<>();
            source.readTypedList(filteredOn, FilteredOn.CREATOR);
        }
        boolean isSortOptionsNull = source.readByte() == (byte) 1;
        if (!isSortOptionsNull) {
            sortOptions = new ArrayList<>();
            source.readTypedList(sortOptions, Option.CREATOR);
        }
        boolean isFilterOptionCategoryNull = source.readByte() == (byte) 1;
        if (!isFilterOptionCategoryNull) {
            filterOptionItems = new ArrayList<>();
            source.readTypedList(filterOptionItems, FilterOptionCategory.CREATOR);
        }
    }

    public static final Parcelable.Creator<ProductListData> CREATOR = new Parcelable.Creator<ProductListData>() {
        @Override
        public ProductListData createFromParcel(Parcel source) {
            return new ProductListData(source);
        }

        @Override
        public ProductListData[] newArray(int size) {
            return new ProductListData[size];
        }
    };

    public String getSortedOn() {
        return sortedOn;
    }

    public void setSortedOn(String sortedOn) {
        this.sortedOn = sortedOn;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<FilterOptionCategory> getFilterOptions() {
        return filterOptionItems;
    }

    public void setFilterOptions(ArrayList<FilterOptionCategory> filterOptions) {
        this.filterOptionItems = filterOptions;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public ArrayList<FilteredOn> getFilteredOn() {
        return filteredOn;
    }

    public void setFilteredOn(ArrayList<FilteredOn> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public ArrayList<Option> getSortOptions() {
        return sortOptions;
    }

    public void setSortOptions(ArrayList<Option> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public boolean isSortSelected() {
        return sortedOn != null;
    }

}
