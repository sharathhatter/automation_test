package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class DynamicPageResponse extends BaseApiResponse {
    @SerializedName(Constants.RESPONSE)
    public GetDynamicPageApiResponse apiResponseContent;
}
