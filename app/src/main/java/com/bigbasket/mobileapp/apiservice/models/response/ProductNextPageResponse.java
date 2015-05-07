package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductNextPageResponse {

    @SerializedName(Constants.PRODUCT_MAP)
    public HashMap<String, ArrayList<Product>> productListMap;
}
