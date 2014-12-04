package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PostVoucherApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.EVOUCHER_MSG)
    public String evoucherMsg;
}
