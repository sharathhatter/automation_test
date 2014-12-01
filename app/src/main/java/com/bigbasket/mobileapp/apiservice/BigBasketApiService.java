package com.bigbasket.mobileapp.apiservice;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowseCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.HomePageApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface BigBasketApiService {

    @GET("/cities/")
    void listCities(Callback<ArrayList<City>> cities);

    @FormUrlEncoded
    @POST("/register-device/")
    void registerDevice(@Field(Constants.DEVICE_ID) String deviceId,
                        @Field(Constants.CITY_ID) String cityId,
                        @Field(Constants.PROPERTIES) String properties,
                        Callback<RegisterDeviceResponse> registerDeviceResponseCallback);

    @GET("/browse-category/")
    void browseCategory(@Query(Constants.VERSION) String version,
                        Callback<ApiResponse<BrowseCategoryApiResponseContent>> browseCategoryApiResponseCallback);

    @GET("/get-home-page/")
    void loadHomePage(Callback<ApiResponse<HomePageApiResponseContent>> homePageApiResponseCallback);

//    @GET("/c-get/")
//    void cartGet()
}
