package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostDeliveryAddressApiResponseContent {

    @SerializedName(Constants.CART_SUMMARY)
    public PostDeliveryAddressCartSummary cartSummary;

    @SerializedName(Constants.SLOTS_INFO)
    public ArrayList<SlotGroup> slotGroupArrayList;

    @SerializedName(Constants.VOUCHERS)
    public ArrayList<ActiveVouchers> activeVouchersArrayList;

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;

    @SerializedName(Constants.EVOUCHER_CODE)
    public String evoucherCode;
}
