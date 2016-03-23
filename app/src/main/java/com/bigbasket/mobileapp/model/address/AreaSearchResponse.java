package com.bigbasket.mobileapp.model.address;

import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Created by muniraju on 29/02/16.
 */
public class AreaSearchResponse extends BaseApiResponse {

    @SerializedName("response")
    AreaSearchResults response;

    public AreaSearchResults getResponse() {
        return response;
    }

    public void setResponse(AreaSearchResults response) {
        this.response = response;
    }
}
