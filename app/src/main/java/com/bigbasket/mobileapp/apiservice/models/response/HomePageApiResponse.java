package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class HomePageApiResponse extends BaseApiResponse {

    @SerializedName(Constants.RESPONSE)
    public HomePageApiResponseContent homePageApiResponseContent;
}
