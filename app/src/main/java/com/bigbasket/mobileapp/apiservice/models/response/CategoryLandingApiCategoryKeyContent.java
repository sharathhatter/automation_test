package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 10/12/14.
 */
public class CategoryLandingApiCategoryKeyContent {

    @SerializedName(Constants.SUB_CATEGORY_ITEMS)
    public SubCategoryModel subCategoryModel;

    @SerializedName(Constants.SUB_CATEGORY_BANNER_IMAGE)
    public ArrayList<String> bannerArrayList;
}
