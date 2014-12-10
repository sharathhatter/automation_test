package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 10/12/14.
 */
public class SubCategoryApiResponse {

    @SerializedName(Constants.VERSION)
    public String responseVersion;

    @SerializedName(Constants.A_OK)
    public boolean a_ok;

    @SerializedName(Constants.CATEGORIES)
    public CategoryLandingApiCategoryKeyContent categoryLandingApiCategoryKeyContent;
}
