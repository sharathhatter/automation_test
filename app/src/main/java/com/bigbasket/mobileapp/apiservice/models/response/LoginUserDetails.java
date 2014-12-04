package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class LoginUserDetails {

    @SerializedName(Constants.FIRSTNAME)
    public String firstName;

    @SerializedName(Constants.LASTNAME)
    public String lastName;

    @SerializedName(Constants.FULL_NAME)
    public String fullName;

    @SerializedName(Constants.GENDER)
    public String gender;

    @SerializedName(Constants.CREATED_ON)
    public String createdOn;

    @SerializedName(Constants.DOB)
    public String dateOfBirth;

    @SerializedName(Constants.MOBILE_NUMBER)
    public String mobileNumber;

    @SerializedName(Constants.HUB)
    public String hub;

    @SerializedName(Constants.ADDITIONAL_ATTRS)
    public HashMap<String, Object> additionalAttrs;
}
