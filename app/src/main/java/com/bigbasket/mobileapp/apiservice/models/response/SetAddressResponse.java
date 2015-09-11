package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SetAddressResponse {
    @SerializedName(Constants.ADDRESSES)
    public ArrayList<AddressSummary> addressSummaries;
}
