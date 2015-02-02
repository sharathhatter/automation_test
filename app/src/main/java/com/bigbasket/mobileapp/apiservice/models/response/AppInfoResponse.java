package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 2/2/15.
 */
public class AppInfoResponse {

    @SerializedName(Constants.UPDATE_INFO)
    public UpdateInfo updateInfo;

}
