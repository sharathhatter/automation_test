package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartGetApiResponseContent {

    @SerializedName(Constants.CART_SUMMARY)
    public CartSummary cartSummary;

    @SerializedName(Constants.FULFILLMENT_INFOS)
    public ArrayList<FulfillmentInfo> fulfillmentInfos;

    @SerializedName(Constants.ANNOTATION_INFO)
    public ArrayList<AnnotationInfo> annotationInfos;

    @SerializedName(Constants.CART_ITEMS)
    public CartGetApiCartItemsContent cartGetApiCartItemsContent;
}
