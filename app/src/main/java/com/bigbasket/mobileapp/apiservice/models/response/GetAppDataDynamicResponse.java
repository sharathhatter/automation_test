package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetAppDataDynamicResponse {
    @SerializedName(Constants.ADD_TO_BASKET_POST_PARAMS)
    public ArrayList<String> addToBasketPostParams;

    @SerializedName(Constants.ADDRESSES)
    public ArrayList<AddressSummary> addressSummaries;

    @SerializedName(Constants.IS_CONTEXTUAL_MODE)
    public boolean isContextualMode;
}
