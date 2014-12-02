package com.bigbasket.mobileapp.apiservice;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowseCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListDetailsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.HomePageApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.QueryMap;

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

    @GET("/c-get/")
    void cartGet(Callback<ApiResponse<CartGetApiResponseContent>> cartGetApiResponseCallback);

    @POST("/c-empty/")
    void emptyCart(Callback<BaseApiResponse> cartEmptyApiResponseCallback);

    @GET("/product-list/")
    void productListUrl(@QueryMap Map<String, String> productQueryMap, Callback<ApiResponse<ProductListData>> productListApiCallback);

    @GET("/browse-promo-cat/")
    void browsePromoCategory(Callback<ApiResponse<BrowsePromoCategoryApiResponseContent>> browsePromoCategoryApiResponseCallback);

    @GET("/get-promo-detail")
    void getPromoDetail(@Query(Constants.PROMO_ID) String promoId, Callback<ApiResponse<PromoDetailApiResponseContent>> promoDetailApiResponseCallback);

    @GET("/get-promo-set-products")
    void getPromoSetProducts(@Query(Constants.PROMO_ID) String promoId, @Query(Constants.SET_ID) String setId,
                             Callback<ApiResponse<PromoSetProductsApiResponseContent>> promoSetProductsApiResponseCallback);

    @GET("/get-promo-summary/")
    void getPromoSummary(@Query(Constants.PROMO_ID) String promoId, Callback<ApiResponse<PromoSummaryApiResponseContent>> promoSummaryApiResponseCallback);

    @FormUrlEncoded
    @POST("/add-promo-bundle/")
    void addPromoBundle(@Field(Constants.PROMO_ID) String promoId, Callback<BaseApiResponse> addPromoBundleApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-get-lists/")
    void getShoppingLists(@Field(Constants.SYSTEM) String isSystemListAlsoNeeded,
                          Callback<GetShoppingListsApiResponse> getShoppingListsApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-edit-list/")
    void editShoppingList(@Field(Constants.SLUG) String shoppingListSlug, @Field("name") String newName,
                          Callback<OldBaseApiResponse> editShoppingListApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-delete-list/")
    void deleteShoppingList(@Field(Constants.SLUG) String shoppingListSlug, Callback<OldBaseApiResponse> deleteShoppingListApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-create-list/")
    void createShoppingList(@Field(Constants.SL_NAME) String shoppingListName, @Field(Constants.IS_PUBLIC) String isPublic,
                            Callback<OldBaseApiResponse> createShoppingListApiResponseCallback);

    @GET("/sl-get-list-summary/")
    void getShoppingListSummary(@Query(Constants.SLUG) String shoppingListSlug, Callback<GetShoppingListSummaryApiResponse> getShoppingListSummaryApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-get-list-details/")
    void getShoppingListDetails(@Field(Constants.SLUG) String shoppingListSlug, @Field(Constants.TOP_CAT_SLUG) String topCategorySlug,
                                Callback<ApiResponse<GetShoppingListDetailsApiResponse>> getShoppingListDetailsApiResponseCallback);
}
