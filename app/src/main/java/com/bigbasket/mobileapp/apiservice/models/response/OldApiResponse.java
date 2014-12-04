package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OldApiResponse<T> extends OldBaseApiResponse {

    @SerializedName(Constants.RESPONSE)
    public T apiResponseContent;
}
