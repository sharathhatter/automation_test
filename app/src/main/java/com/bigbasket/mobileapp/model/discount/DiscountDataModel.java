package com.bigbasket.mobileapp.model.discount;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 12/6/15.
 */
public class DiscountDataModel {

    @SerializedName(Constants.CATEGORY_DISCOUNT)
    public SectionData categoryDiscount;

    @SerializedName(Constants.PERCENTAGE_DISCOUNT)
    public SectionData percentageDiscount;
}
