package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductQuery implements Parcelable {

    private String type;
    private String slug;
    private String sortedOn;
    private boolean _wasSortedOnNull;
    private Map<String, Set<String>> filteredOn;
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

    public ProductQuery(String type, String slug, String sortedOn, Map<String, Set<String>> filteredOn, int page) {
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

    public Map<String, Set<String>> getFilteredOn() {
        return filteredOn;
    }

    public void setFilteredOn(Map<String, Set<String>> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<NameValuePair> getAsNameValuePair() {
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair(Constants.TYPE, type));
        nameValuePairList.add(new BasicNameValuePair(Constants.SLUG, slug));
        nameValuePairList.add(new BasicNameValuePair(Constants.CURRENT_PAGE, String.valueOf(page)));
        if (!TextUtils.isEmpty(sortedOn)) {
            nameValuePairList.add(new BasicNameValuePair(Constants.SORT_ON, sortedOn));
        }
        if (filteredOn != null && !filteredOn.isEmpty()) {
            JSONArray filterJsonArray = getFilteredOnJsonArray(filteredOn);
            nameValuePairList.add(new BasicNameValuePair(Constants.FILTER_ON,
                    filterJsonArray.toString()));
        }
        return nameValuePairList;
    }

    private JSONArray getFilteredOnJsonArray(Map<String, Set<String>> filteredOnMap) {
        JSONArray filterJsonArray = new JSONArray();
        for (Map.Entry<String, Set<String>> filterEntry : filteredOnMap.entrySet()) {
            if (filterEntry.getValue().size() != 0) {
                JSONArray array = new JSONArray();
                for (String key : filterEntry.getValue()) {
                    array.put(key);
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("filter_slug", filterEntry.getKey());
                    jsonObject.put("values", array);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                filterJsonArray.put(jsonObject);
            }
        }
        return filterJsonArray;
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
