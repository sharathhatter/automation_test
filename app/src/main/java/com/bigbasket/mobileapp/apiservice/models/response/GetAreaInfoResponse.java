package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class GetAreaInfoResponse {

    @SerializedName(Constants.PIN_CODE_MAP)
    public HashMap<String, ArrayList<String>> pinCodeMaps;
}
