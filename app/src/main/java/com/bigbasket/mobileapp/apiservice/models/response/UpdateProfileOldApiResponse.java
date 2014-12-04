package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 4/12/14.
 */
public class UpdateProfileOldApiResponse {

    @SerializedName(Constants.MEMBER_DETAILS)
    public UpdateProfileModel memberDetails;

    public String status;
    public String message;

    @SerializedName(Constants.ERROR_TYPE)
    public String errorType;
}
