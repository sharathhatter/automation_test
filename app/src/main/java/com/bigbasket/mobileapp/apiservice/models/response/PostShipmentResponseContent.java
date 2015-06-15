package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostShipmentResponseContent {

    @SerializedName(Constants.VOUCHERS)
    public ArrayList<ActiveVouchers> activeVouchersArrayList;

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;

    @SerializedName(Constants.EVOUCHER_CODE)
    public String evoucherCode;

    @SerializedName(Constants.CREDIT_DETAILS)
    public ArrayList<CreditDetails> creditDetails;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderDetails orderDetails;
}
