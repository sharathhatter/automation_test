package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.general.ShopInShop;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BrowseCategoryApiResponseContent {
    public String version;

    @SerializedName(Constants.A_OK)
    public boolean aOk;

    @SerializedName(Constants.CATEGORIES)
    public ArrayList<TopCategoryModel> topCategoryModels;

    @SerializedName(Constants.HAS_BUNDLE_PACK)
    public boolean hasBundlePack;

    public ArrayList<ShopInShop> shops;
}
