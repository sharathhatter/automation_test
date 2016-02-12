package com.bigbasket.mobileapp.model.product;

/**
 * Created by manu on 22/2/16.
 */

import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductTag {
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("values")
    @Expose
    private ArrayList<HashMap<String, DestinationInfo>> values = new ArrayList<>();

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
}
