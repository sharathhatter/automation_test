package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by manu on 14/9/15.
 */
public class GetPaytmPaymentParams {

    @SerializedName(Constants.POST_PARAMS)
    public PaytmPostParams paytmPostParams;

}