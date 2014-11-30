package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BrowseCategoryApiResponse extends BaseApiResponse {

    @SerializedName(Constants.RESPONSE)
    private BrowseCategoryApiResponseContent browseCategoryApiResponseContent;

    public BrowseCategoryApiResponseContent getBrowseCategoryApiResponseContent() {
        return browseCategoryApiResponseContent;
    }
}
