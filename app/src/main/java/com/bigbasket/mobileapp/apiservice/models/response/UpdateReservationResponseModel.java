package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 4/2/15.
 */
public class UpdateReservationResponseModel<T> extends  OldBaseApiResponse{

    @SerializedName(Constants.RESPONSE)
    public T apiResponseContent;
}
