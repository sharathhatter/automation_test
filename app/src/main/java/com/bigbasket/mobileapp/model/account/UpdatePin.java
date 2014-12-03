package com.bigbasket.mobileapp.model.account;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 2/12/14.
 */
public class UpdatePin {

    @SerializedName(Constants.CURRENT_PIN)
    public String currentPin;
}
