package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductListData implements Parcelable {

    private String sortedOn;
    private String userSortedOn;
    private int productCount;
    private int currentPage;
    private int totalPages;
    private String query;
    private List<Product> products;
    private Map<String, Set<String>> filteredOn;
    private ArrayList<Option> sortOptions;
    private boolean isFilterSelected = false;
    private ArrayList<FilterOptionCategory> filterOptionItems;
    private String sortedOnDisplay;

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
        boolean isUserSortedOnNull = userSortedOn == null;
        dest.writeByte(isUserSortedOnNull ? (byte) 1 : (byte) 0);
        if (!isUserSortedOnNull) {
            dest.writeString(userSortedOn);
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
            ArrayList<String> filteredOnMapKeyList = new ArrayList<>();
            ArrayList<Set<String>> filteredOnMapValueList = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entrySet : filteredOn.entrySet()) {
                filteredOnMapKeyList.add(entrySet.getKey());
                filteredOnMapValueList.add(entrySet.getValue());
            }
            dest.writeStringList(filteredOnMapKeyList);
            dest.writeValue(filteredOnMapValueList);
        }
        boolean isSortOptionsNull = sortOptions == null;
        dest.writeByte(isSortOptionsNull ? (byte) 1 : (byte) 0);
        if (!isSortOptionsNull) {
            dest.writeTypedList(sortOptions);
        }
        dest.writeByte(isFilterSelected ? (byte) 1 : (byte) 0);
        boolean isFilterOptionCategoryNull = filterOptionItems == null;
        dest.writeByte(isFilterOptionCategoryNull ? (byte) 1 : (byte) 0);
        if (!isFilterOptionCategoryNull) {
            dest.writeTypedList(filterOptionItems);
        }
        boolean isSortedOnDisplayNull = sortedOnDisplay == null;
        dest.writeByte(isSortedOnDisplayNull ? (byte) 1 : (byte) 0);
        if (!isSortedOnDisplayNull) {
            dest.writeString(sortedOnDisplay);
        }
    }

    public ProductListData(Parcel source) {
        boolean isSortedOnNull = source.readByte() == (byte) 1;
        if (!isSortedOnNull) {
            sortedOn = source.readString();
        }
        boolean isUserSortedOnNull = source.readByte() == (byte) 1;
        if (!isUserSortedOnNull) {
            userSortedOn = source.readString();
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
            ArrayList<String> filteredOnMapKeyList = new ArrayList<>();
            ArrayList<Set<String>> filteredOnMapValueList;
            source.readStringList(filteredOnMapKeyList);
            filteredOnMapValueList = (ArrayList<Set<String>>) source.readValue(ArrayList.class.getClassLoader());
            filteredOn = new HashMap<>();
            for (int i = 0; i < filteredOnMapKeyList.size(); i++) {
                filteredOn.put(filteredOnMapKeyList.get(i), filteredOnMapValueList.get(i));
            }
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
        boolean isSortedOnDisplayNull = source.readByte() == (byte) 1;
        if (!isSortedOnDisplayNull) {
            sortedOnDisplay = source.readString();
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

    public String getSortedOnDisplay() {
        return sortedOnDisplay;
    }

    public void setSortedOnDisplay(String sortedOnDisplay) {
        this.sortedOnDisplay = sortedOnDisplay;
    }

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

    public Map<String, Set<String>> getFilteredOn() {
        return filteredOn;
    }

    public void setFilteredOn(Map<String, Set<String>> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public ArrayList<Option> getSortOptions() {
        return sortOptions;
    }

    public void setSortOptions(ArrayList<Option> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public boolean isFilterSelected() {
        return isFilterSelected;
    }

    public void setFilterSelected(boolean filterSelected) {
        isFilterSelected = filterSelected;
    }

    public boolean isSortSelected() {
        return sortedOn != null;
    }

    public String getUserSortedOn() {
        return userSortedOn;
    }

    public void setUserSortedOn(String userSortedOn) {
        this.userSortedOn = userSortedOn;
    }

    public boolean isUserSortSelected() {
        return userSortedOn != null;
    }

}
