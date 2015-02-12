package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class CategoryLandingApiCategoryKeyContent extends GetDynamicPageApiResponse {

    @SerializedName(Constants.SUB_CATEGORY_ITEMS)
    public SubCategoryModel subCategoryModel;
}
