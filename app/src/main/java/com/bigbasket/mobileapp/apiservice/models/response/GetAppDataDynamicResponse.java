package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class GetAppDataDynamicResponse {
    @SerializedName(Constants.ADD_TO_BASKET_POST_PARAMS)
    public ArrayList<String> addToBasketPostParams;

    @SerializedName(Constants.ADDRESSES)
    public ArrayList<AddressSummary> addressSummaries;

    @SerializedName(Constants.IS_CONTEXTUAL_MODE)
    public boolean isContextualMode;

    @SerializedName(Constants.EXPRESS_AVAILABILITY)
    public String expressAvailability;

    @SerializedName(Constants.MODE_NAME)
    public String abModeName;

    @SerializedName(Constants.STORE_AVAILABILITY_MAP)
    public HashMap<String, String> storeAvailabilityMap;
}
