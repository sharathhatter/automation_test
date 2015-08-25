package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostDeliveryAddressApiResponseContent {
    @SerializedName(Constants.QC_HAS_VALIDATION_ERRORS)
    public boolean hasQcErrors;
    @SerializedName(Constants.QC_VALIDATION_ERROR_DATA)
    public ArrayList<QCErrorData> qcErrorDatas;
    public String title;
    public String msg;
}
