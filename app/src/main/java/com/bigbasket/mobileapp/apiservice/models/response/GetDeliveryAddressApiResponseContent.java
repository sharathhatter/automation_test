package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetDeliveryAddressApiResponseContent {

    @SerializedName(Constants.ADDRESSES)
    public ArrayList<Address> addresses;
}
