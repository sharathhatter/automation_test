package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 2/2/15.
 */
public class UpdateInfo {

    @SerializedName(Constants.IS_UPDATE_REQUIRED)
    public int isUpdateRequired;

    @SerializedName(Constants.APP_EXPIRE_BY)
    public String appExpiredBy;
}
