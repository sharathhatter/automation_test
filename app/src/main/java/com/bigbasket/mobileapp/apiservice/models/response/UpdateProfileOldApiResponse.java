package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class UpdateProfileOldApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.MEMBER_DETAILS)
    public UpdateProfileModel memberDetails;
}
