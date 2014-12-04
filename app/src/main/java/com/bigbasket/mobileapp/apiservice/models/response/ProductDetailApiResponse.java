package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ProductDetailApiResponse extends OldBaseApiResponse {

    @SerializedName(Constants.PRODUCT)
    public Product product;
}
