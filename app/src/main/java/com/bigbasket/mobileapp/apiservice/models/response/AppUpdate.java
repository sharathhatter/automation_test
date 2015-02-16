package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 9/2/15.
 */
public class AppUpdate {
    @SerializedName(Constants.V_EXPIRY_DATE)
    public String expiryDate;

    @SerializedName(Constants.UPGRADE_MSG)
    public String upgradeMsg;

    @SerializedName(Constants.LATEST_APP_VERSION)
    public String latestAppVersion;
}
