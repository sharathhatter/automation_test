package com.bigbasket.mobileapp.model.product;

/**
 * Created by manu on 22/2/16.
 */

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductTag implements Parcelable {
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("values")
    @Expose
    private ArrayList<HashMap<String, DestinationInfo>> values;

    /**
     * @return The type
     */
    public Integer getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return The header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header The header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<HashMap<String, DestinationInfo>> getValues() {
        return values;
    }

    public void setValues(ArrayList<HashMap<String, DestinationInfo>> values) {
        this.values = values;
    }

    public static final Parcelable.Creator<ProductTag> CREATOR = new Parcelable.Creator<ProductTag>() {
        @Override
        public ProductTag createFromParcel(Parcel source) {
            return new ProductTag(source);
        }

        @Override
        public ProductTag[] newArray(int size) {
            return new ProductTag[size];
        }
    };

    public ProductTag(Parcel source) {
        type = source.readInt();
        header = source.readString();
        int valuesSize = source.readInt();
        if (valuesSize > 0) {
            values = new ArrayList<HashMap<String, DestinationInfo>>(valuesSize);
            for (int i = 0; i < valuesSize; i++) {
                Bundle b = source.readBundle(getClass().getClassLoader());
                int bundleSize = b.keySet().size();
                HashMap<String, DestinationInfo> destinationInfoHashMap = new HashMap<>(bundleSize);
                for (String key : b.keySet()) {
                    destinationInfoHashMap.put(key, (DestinationInfo) b.getParcelable(key));
                }
                values.add(destinationInfoHashMap);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(header);
        if (values != null && !values.isEmpty()) {
            dest.writeInt(values.size());
            for (HashMap<String, DestinationInfo> v : values) {
                Bundle bundle = new Bundle();
                for (Map.Entry<String, DestinationInfo> e : v.entrySet()) {
                    bundle.putParcelable(e.getKey(), e.getValue());
                }
                dest.writeBundle(bundle);
            }
        } else {
            dest.writeInt(0);
        }
    }
}
