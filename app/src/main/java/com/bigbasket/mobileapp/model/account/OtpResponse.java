package com.bigbasket.mobileapp.model.account;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 17/12/15.
 */
public class OtpResponse {

    @SerializedName(Constants.OTP_CODE)
    public String otpCode;
}
