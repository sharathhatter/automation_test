package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetPaymentTypes {

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;
}
