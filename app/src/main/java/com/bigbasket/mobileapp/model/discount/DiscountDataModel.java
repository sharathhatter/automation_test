package com.bigbasket.mobileapp.model.discount;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class DiscountDataModel {

    @SerializedName(Constants.CATEGORY_DISCOUNT_INFO)
    public SectionData categoryDiscount;

    @SerializedName(Constants.BIN_DISCOUNT_INFO)
    public SectionData percentageDiscount;
}
