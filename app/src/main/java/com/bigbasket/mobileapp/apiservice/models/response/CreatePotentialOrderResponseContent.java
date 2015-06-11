package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
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

    @SerializedName(Constants.VOUCHERS)
    public ArrayList<ActiveVouchers> activeVouchersArrayList;

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;

    @SerializedName(Constants.EVOUCHER_CODE)
    public String evoucherCode;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderDetails orderDetails;
    @SerializedName(Constants.CREDIT_DETAILS)
    public ArrayList<CreditDetails> creditDetails;
}
