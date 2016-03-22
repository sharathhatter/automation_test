package com.bigbasket.mobileapp.model.address;

import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Created by muniraju on 29/02/16.
 */
public class AreaSearchResults  {

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("results")
    private AreaSearchResult[] results;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public AreaSearchResult[] getResults() {
        return results;
    }

    public void setResults(AreaSearchResult[] results) {
        this.results = results;
    }
}
