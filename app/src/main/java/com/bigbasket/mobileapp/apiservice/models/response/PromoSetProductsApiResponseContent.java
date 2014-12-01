package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PromoSetProductsApiResponseContent {

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImgUrl;

    @SerializedName(Constants.PROMO_SET_PRODUCTS)
    public ArrayList<Product> promoSetProducts;
}
