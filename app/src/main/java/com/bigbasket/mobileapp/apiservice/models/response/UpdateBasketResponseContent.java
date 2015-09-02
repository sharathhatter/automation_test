package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class UpdateBasketResponseContent {
    @SerializedName(Constants.CITY)
    public City city;
}
