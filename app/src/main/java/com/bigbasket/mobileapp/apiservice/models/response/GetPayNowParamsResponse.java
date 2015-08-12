package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.OrderInvoiceDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetPayNowParamsResponse {
    public String amount;

    @SerializedName(Constants.ORDER_NUMBER)
    public String orderNumber;

    @SerializedName(Constants.INVOICE_NUMBER)
    public String invoiceNumber;

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;

    @SerializedName(Constants.CREDIT_DETAILS)
    public ArrayList<CreditDetails> creditDetails;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderInvoiceDetails orderDetails;
}
