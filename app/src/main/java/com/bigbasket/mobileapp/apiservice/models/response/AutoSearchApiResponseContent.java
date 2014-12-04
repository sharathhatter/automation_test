package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class AutoSearchApiResponseContent {

    @SerializedName(Constants.TC)
    public AutoSearchResponse autoSearchResponse;
}
