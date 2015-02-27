package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ProductListApiResponse extends GetDynamicPageApiResponse {

    @SerializedName(Constants.PRODUCT_INFO)
    public ProductListData productListData;
}
