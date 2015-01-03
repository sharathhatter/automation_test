package com.bigbasket.mobileapp.apiservice;

import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AutoSearchApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowseCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartOperationApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentParamsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetProductsForOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListDetailsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.HomePageApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostFeedbackApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrescriptionImageUrls;
import com.bigbasket.mobileapp.apiservice.models.response.ProductDetailApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SubCategoryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileOldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.account.CurrentWalletBalance;
import com.bigbasket.mobileapp.model.account.UpdatePin;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.account.spendTrends.SpendTrends;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.PrescriptionId;
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

    @GET("/c-get/")
    void cartGetForIds(@Query(Constants.FULFILLMENT_ID) String fulfillmentId,
                       Callback<ApiResponse<CartGetApiResponseContent>> cartGetApiResponseCallback);

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

    @GET("/get-pin/")
    void getCurrentMemberPin(Callback<ApiResponse<UpdatePin>> updatePinCallback);

    @FormUrlEncoded
    @POST("/change-pin/")
    void updateCurrentMemberPin(@Field(Constants.NEW_PIN) String newPin, Callback<BaseApiResponse> updatePinCallback);

    @FormUrlEncoded
    @POST("/upload-prescription/")
    void uploadPrescription(@Field(Constants.PATIENT_NAME) String patientName,
                            @Field(Constants.DOCTOR_NAME) String docName,
                            @Field(Constants.PRESCRIPTION_NAME) String PrescriptionName,
                            Callback<ApiResponse<PrescriptionId>> prescriptionId);

    @FormUrlEncoded
    @POST("/upload-prescription/")
    void uploadPrescriptionImages(@Field(Constants.PHARMA_PRESCRIPTION_ID) String prescriptionId,
                                  @Field(Constants.CHUNK_NUMBER) String chunkNumber,
                                  @Field(Constants.MAX_CHUNKS) String maxChunk,
                                  @Field(Constants.PRESCRIPTION_IMAGE_CHUNK) String prescriptionImageChunk,
                                  @Field(Constants.IMAGE_SEQUENCE) String imageSeq,
                                  Callback<BaseApiResponse> uploadPrescriptionImageCallback);

    @GET("/get-prescription-images/")
    void getPrescriptionImageUrls(@Query(Constants.PHARMA_PRESCRIPTION_ID) String prescriptionId,
                                  Callback<ApiResponse<PrescriptionImageUrls>> imageUrlsCallback);

    @GET("/get-current-wallet-balance/")
    void getCurrentWalletBalance(Callback<ApiResponse<CurrentWalletBalance>> currentWalletBalCallback);

    @GET("/get-wallet-activity/")
    void getWalletActivity(@Query(Constants.DATE_FROM) String dateFrom,
                           @Query(Constants.DATE_TO) String dateTo,
                           Callback<ApiResponse<ArrayList<WalletDataItem>>> walletActivityCallback);

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
    void getShoppingListSummary(@Query(Constants.SLUG) String shoppingListSlug,
                                Callback<GetShoppingListSummaryApiResponse> getShoppingListSummaryApiResponseCallback);

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
    @POST("/change-password/")
    void changePassword(@Field(Constants.OLD_PASSWORD) String oldPassword,
                        @Field(Constants.NEW_PASSWORD) String newPassword,
                        @Field(Constants.CONFIRM_PASSWORD) String confirmPassword,
                        Callback<OldBaseApiResponse> changePasswordCallback);

    @GET("/update-profile/")
    void getMemberProfileData(Callback<UpdateProfileOldApiResponse> memberProfileDataCallback);

    @FormUrlEncoded
    @POST("/update-profile/")
    void setUserDetailsData(@Field(Constants.USER_DETAILS) String userDetails,
                            Callback<UpdateProfileOldApiResponse> changePasswordCallback);

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
                           @Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType,
                           @Field(Constants.SOCIAL_LOGIN_PARAMS) String socialLoginParams,
                           Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/social-register-member/")
    void socialRegisterMember(@Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType,
                              @Field(Constants.SOCIAL_LOGIN_PARAMS) String socialLoginParams,
                              Callback<LoginApiResponse> loginApiResponseContent);

    @FormUrlEncoded
    @POST("/register-member/")
    void registerMember(@Field(Constants.USER_DETAILS) String userDetails, Callback<LoginApiResponse> loginApiResponseCallback);

    @FormUrlEncoded
    @POST("/create-address/")
    void createAddress(@FieldMap HashMap<String, String> params,
                       Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> createUpdateAddressApiResponseCallback);


    @FormUrlEncoded
    @POST("/update-address/")
    void updateAddress(@FieldMap HashMap<String, String> params,
                       Callback<ApiResponse<CreateUpdateAddressApiResponseContent>> createUpdateAddressApiResponseCallback);

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
    void basketCheck(Callback<ApiResponse<MarketPlace>> marketPlaceApiResponseCallback);

    @GET("/c-summary/")
    void cartSummary(Callback<CartSummaryApiResponse> cartSummaryApiResponseCallback);

    @GET("/product-details/")
    void productDetails(@Query(Constants.PROD_ID) String productId, Callback<ProductDetailApiResponse> productDetailApiResponseCallback);

    @GET("/co-get-delivery-addresses/")
    void getDeliveryAddresses(Callback<ApiResponse<GetDeliveryAddressApiResponseContent>> getDeliveryAddressApiResponseCallback);

    @FormUrlEncoded
    @POST("/co-post-delivery-addresses/")
    void postDeliveryAddresses(@Field(Constants.P_ORDER_ID) String potentialOrderId, @Field(Constants.ADDRESS_ID) String addressId,
                               @Field(Constants.SUPPORT_CC) String supportsCreditCard,
                               Callback<OldApiResponse<PostDeliveryAddressApiResponseContent>> postDeliveryAddressApiResponseCallback);

    @FormUrlEncoded
    @POST("/co-post-slot-and-payment/")
    void postSlotAndPayment(@Field(Constants.P_ORDER_ID) String potentialOrderId, @Field(Constants.SLOTS) String slots,
                            @Field(Constants.PAYMENT_TYPE) String paymentType, @Field(Constants.SUPPORT_CC) String supportsCreditCard,
                            Callback<ApiResponse<OrderSummary>> postSlotAndPaymentApiResponseCallback);

    @GET("/get-payment-params/")
    void getPaymentParams(@Query(Constants.PID) String potentialOrderId, @Query(Constants.AMOUNT) String amount,
                          Callback<ApiResponse<GetPaymentParamsApiResponseContent>> getPaymentParamsApiResponseCallback);

    @FormUrlEncoded
    @POST("/co-place-order/")
    void placeOrder(@Field(Constants.P_ORDER_ID) String potentialOrderId, @Field(Constants.TXN_ID) String txnId,
                    Callback<OldApiResponse<PlaceOrderApiResponseContent>> placeOrderApiResponseCallback);

    @GET("/c-bulk-remove/")
    void cartBulkRemove(@Query(Constants.FULFILLMENT_ID) String fulfillmentId,
                        Callback<BaseApiResponse> cartBulkRemoveApiResponseCallback);

    @GET("/search-tc/")
    ApiResponse<AutoSearchApiResponseContent> autoSearch(@Query("t") String term);

    @FormUrlEncoded
    @POST("/co-post-voucher/")
    void postVoucher(@Field(Constants.P_ORDER_ID) String potentialOrderId, @Field(Constants.EVOUCHER_CODE) String evoucherCode,
                     Callback<PostVoucherApiResponse> postVoucherApiResponseCallback);

    @GET("/change-city/")
    void changeCity(@Query("new_city_id") String newCityId, Callback<OldBaseApiResponse> oldBaseApiResponseCallback);

    @GET("/get-area-info/")
    void getAreaInfo(CallbackGetAreaInfo callbackGetAreaInfo);

    @FormUrlEncoded
    @POST("/post-case-feedback/")
    void postCaseFeedback(@Field(Constants.CASE_ID) String caseId, @Field(Constants.RATING) String rating,
                          @Field(Constants.COMMENTS) String comments,
                          Callback<ApiResponse<PostFeedbackApiResponseContent>> postFeedbackApiResponseCallback);

    @FormUrlEncoded
    @POST("/update-version-number/")
    void updateVersionNumber(@Field(Constants.DEVICE_ID) String deviceId,
                             @Field(Constants.APP_VERSION) String appVersion,
                             Callback<ApiResponse<UpdateVersionInfoApiResponseContent>> updateVersionInfoApiResponseCallback);

    @GET("/category-landing/")
    void getSubCategoryData(@Query(Constants.CATEGORY_SLUG) String categorySlug,
                            @Query(Constants.VERSION) String version,
                            Callback<ApiResponse<SubCategoryApiResponse>> subCategoryCallback);

    @FormUrlEncoded
    @POST("/co-update-reservation/")
    void coUpdateReservation(@Field(Constants.P_ORDER_ID) String potentialOrderId,
                             @Field(Constants.ITEMS) String items,
                             Callback<OldBaseApiResponse> coUpdateReservationApiResponseCallback);

    @FormUrlEncoded
    @POST("/co-reserve-quantity/")
    void coReserveQuantity(@Field(Constants.PHARMA_PRESCRIPTION_ID) String pharmaPrescriptionId,
                           Callback<OldApiResponse<COReserveQuantity>> coReserveQtyApiResponseCallback);

    @FormUrlEncoded
    @POST("/update-location/")
    void updateLocation(@Field(Constants.ADDRESS_ID) String addressId, @Field(Constants.LAT) double lat,
                        @Field(Constants.LNG) double lng, Callback<BaseApiResponse> updateLocationApiResponseCallback);

    @GET("/get-products-for-order/")
    void getProductsForOrder(@Query(Constants.ORDER_ID) String orderId,
                             Callback<ApiResponse<GetProductsForOrderApiResponseContent>> getProductsForOrderApiResponseCallback);

    @GET("/spend-trends/")
    void spendTrends(Callback<ApiResponse<SpendTrends>> spendTrendsApiResponseCallback);
}
