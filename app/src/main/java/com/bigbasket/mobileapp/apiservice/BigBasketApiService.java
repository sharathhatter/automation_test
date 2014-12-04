package com.bigbasket.mobileapp.apiservice;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowseCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartOperationApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListDetailsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.HomePageApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductDetailApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FieldMap;
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

    @FormUrlEncoded
    @POST("/c-incr-i/")
    void incrementCartItem(@Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                           Callback<CartOperationApiResponse> cartOperationApiResponseCallback);

    @FormUrlEncoded
    @POST("/c-decr-i/")
    void decrementCartItem(@Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                           Callback<CartOperationApiResponse> cartOperationApiResponseCallback);

    @FormUrlEncoded
    @POST("/c-set-i/")
    void setCartItem(@Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                     Callback<CartOperationApiResponse> cartOperationApiResponseCallback);

    @FormUrlEncoded
    @POST("/login/")
    void login(@Field(Constants.EMAIL) String email, @Field(Constants.PASSWORD) String password,
               Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/social-login/")
    void socialLogin(@Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType, @Field(Constants.SOCIAL_LOGIN_PARAMS) String socialLoginParams,
                     Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/social-link-account/")
    void socialLinkAccount(@Field(Constants.EMAIL) String email, @Field(Constants.PASSWORD) String password,
                           @Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType, @Field(Constants.SOCIAL_LOGIN_PARAMS) String socialLoginParams,
                           Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/social-register-member/")
    void socialRegisterMember(@Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType, @Field(Constants.SOCIAL_LOGIN_PARAMS) String socialLoginParams,
                              Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/register-member/")
    void registerMember(@Field(Constants.USER_DETAILS) String userDetails, Callback<LoginApiResponse> loginApiResponseCallback);

    @FormUrlEncoded
    @POST("/create-address/")
    void createAddress(@FieldMap HashMap<String, String> params, Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> createUpdateAddressApiResponseCallback);


    @FormUrlEncoded
    @POST("/update-address/")
    void updateAddress(@FieldMap HashMap<String, String> params, Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> createUpdateAddressApiResponseCallback);

    @GET("/get-orders/")
    void getOrders(@Query(Constants.ORDER_TYPE) String orderType, @Query(Constants.ORDER_RANGE) String orderRange,
                   Callback<OrderListApiResponse> orderListApiResponseCallback);

    @GET("/get-invoice/")
    void getInvoice(@Query(Constants.ORDER_ID) String orderId, Callback<ApiResponse<OrderInvoice>> getInvoiceApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-add-item/")
    void addItemToShoppingList(@Field("product_id") String productId, @Field("slugs") String shoppingListSlugs,
                               Callback<OldBaseApiResponse> addItemToShoppingListApiResponseCallback);

    @FormUrlEncoded
    @POST("/sl-delete-item/")
    void deleteItemFromShoppingList(@Field("product_id") String productId, @Field(Constants.SLUG) String shoppingListSlug,
                                    Callback<OldBaseApiResponse> deleteItemFromShoppingListApiResponseCallback);

    @GET("/co-basket-check/")
    void checkoutBasketCheck(Callback<ApiResponse<MarketPlace>> marketPlaceApiResponseCallback);

    @GET("/c-summary/")
    void cartSummary(Callback<CartSummaryApiResponse> cartSummaryApiResponseCallback);

    @GET("/product-details/")
    void productDetails(@Query(Constants.PROD_ID) String productId, Callback<ProductDetailApiResponse> productDetailApiResponseCallback);
}
