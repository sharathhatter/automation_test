package com.bigbasket.mobileapp.util;

import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class ParserUtil {

    public static ArrayList<TopCategoryModel> parseTopCategory(JsonArray topCategoryArray) {
        ArrayList<TopCategoryModel> topCategoryModels;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<TopCategoryModel>>() {
        }.getType();
        topCategoryModels = gson.fromJson(topCategoryArray, collectionType);
        return topCategoryModels;
    }

    public static ArrayList<Product> parseProductList(String productListStr) {
        ArrayList<Product> products;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Product>>() {
        }.getType();
        products = gson.fromJson(productListStr, collectionType);
        return products;
    }
}
