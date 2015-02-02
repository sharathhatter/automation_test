package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jugal on 10/12/14.
 */
public class CategoryLandingApiCategoryKeyContent<T> {

    @SerializedName(Constants.SUB_CATEGORY_ITEMS)
    public SubCategoryModel subCategoryModel;

    @SerializedName(Constants.SECTION_DATA)
    public ArrayList<SectionData> sectionData;
}
