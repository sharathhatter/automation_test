package com.bigbasket.mobileapp.apiservice.models.response;

import android.text.TextUtils;

import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OldBaseApiResponse {
    public String status;
    public String message;

    @SerializedName(Constants.ERROR_TYPE)
    public String errorType;

    public int getErrorTypeAsInt() {
        if (TextUtils.isEmpty(errorType)) {
            return ApiErrorCodes.INTERNAL_SERVER_ERROR;
        }
        return Integer.parseInt(errorType);
    }
}
