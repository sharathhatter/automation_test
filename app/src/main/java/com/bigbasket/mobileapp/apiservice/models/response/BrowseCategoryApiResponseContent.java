package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BrowseCategoryApiResponseContent {
    private String version;

    @SerializedName(Constants.A_OK)
    private boolean aOk;

    @SerializedName(Constants.CATEGORIES)
    private ArrayList<TopCategoryModel> topCategoryModels;

    public String getVersion() {
        return version;
    }

    public boolean isaOk() {
        return aOk;
    }

    public ArrayList<TopCategoryModel> getTopCategoryModels() {
        return topCategoryModels;
    }
}
