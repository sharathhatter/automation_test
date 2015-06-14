package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.shipments.Shipment;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CreatePotentialOrderResponseContent {
    @SerializedName(Constants.P_ORDER_ID)
    public String potentialOrderId;
    @SerializedName(Constants.QC_HAS_VALIDATION_ERRORS)
    public boolean hasQcErrors;
    @SerializedName(Constants.QC_VALIDATION_ERROR_DATA)
    public ArrayList<QCErrorData> qcErrorDatas;
    public ArrayList<Shipment> shipments;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderDetails orderDetails;
}
