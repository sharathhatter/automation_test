package com.bigbasket.mobileapp.apiservice;

import com.bigbasket.mobileapp.apiservice.models.response.AddAllShoppingListItemResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AppDataResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AutoSearchApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowsePromoCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CartInfo;
import com.bigbasket.mobileapp.apiservice.models.response.CartOperationApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartSummaryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.CreateUpdateAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetAreaInfoResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayNowParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentTypes;
import com.bigbasket.mobileapp.apiservice.models.response.GetProductsForOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponseWithCart;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OrderListApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiPayZappResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiPrePaymentResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostFeedbackApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostGiftItemsResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostShipmentResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductDetailApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductNextPageResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressTransientResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SubCategoryApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.ValidateOrderPaymentApiResponse;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.account.CurrentWalletBalance;
import com.bigbasket.mobileapp.model.account.OtpResponse;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.ads.AdAnalyticsData;
import com.bigbasket.mobileapp.model.ads.SponsoredAds;
import com.bigbasket.mobileapp.model.discount.DiscountDataModel;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityShopsListData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface BigBasketApiService {

    @GET("cities/")
    Call<ArrayList<City>> listCities();

    @FormUrlEncoded
    @POST("register-device/")
    Call<RegisterDeviceResponse> registerDevice(@Field(Constants.DEVICE_IMEI) String imei,
                                                @Field(Constants.DEVICE_ID) String deviceId,
                                                @Field(Constants.CITY_ID) String cityId,
                                                @Field(Constants.PROPERTIES) String properties);

    @GET("get-dynamic-page/")
    Call<ApiResponse<GetDynamicPageApiResponse>> getDynamicPage(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                @Query(Constants.OS) String osName,
                                                                @Query(Constants.APP_VERSION) String version,
                                                                @Query(Constants.SCREEN) String screen);

    @GET("c-get/")
    Call<ApiResponse<CartGetApiResponseContent>> cartGet(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                         @Query(Constants.FULFILLMENT_ID) String fulfillmentId);

    @POST("c-empty/")
    Call<BaseApiResponse> emptyCart();

    @GET("product-list/")
    Call<ApiResponse<ProductTabData>> productList(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                  @QueryMap Map<String, String> productQueryMap);

    @GET("sponsored-items/")
    Call<ApiResponse<SponsoredAds>> getSponsoredProducts(
            @Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
            @Query(Constants.TYPE) String type,
            @Query(Constants.SLUG) String slug,
            @Query(Constants.TAB_TYPE) String tabType,
            @QueryMap Map<String, String> productQueryMap);

    @Headers({"Content-Type: application/json"})
    @POST("ads-analytics/")
    Call<BaseApiResponse> postAdAnalytics(@Body AdAnalyticsData[] adAnalyticsData);


    @GET("store-list/")
    Call<ApiResponse<SpecialityShopsListData>> getSpecialityShops(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                  @Query(Constants.CATEGORY) String categoryValue);

    @GET("product-next-page/")
    Call<ApiResponse<ProductNextPageResponse>> productNextPage(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                               @QueryMap Map<String, String> productQueryMap);

    @GET("browse-promo-cat/")
    Call<ApiResponse<BrowsePromoCategoryApiResponseContent>> browsePromoCategory(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @GET("get-promo-detail")
    Call<ApiResponse<PromoDetailApiResponseContent>> getPromoDetail(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                    @Query(Constants.PROMO_ID) String promoId);

    @GET("get-promo-set-products")
    Call<ApiResponse<PromoSetProductsApiResponseContent>> getPromoSetProducts(@Query(Constants.PROMO_ID) String promoId,
                                                                              @Query(Constants.SET_ID) String setId);

    @GET("get-promo-summary/")
    Call<ApiResponse<PromoSummaryApiResponseContent>> getPromoSummary(@Query(Constants.PROMO_ID) String promoId);

    @FormUrlEncoded
    @POST("add-promo-bundle/")
    Call<ApiResponse<CartInfo>> addPromoBundle(@Field(Constants.PROMO_ID) String promoId);

    @GET("get-current-wallet-balance/")
    Call<ApiResponse<CurrentWalletBalance>> getCurrentWalletBalance(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @GET("get-wallet-activity/")
    Call<ApiResponse<ArrayList<WalletDataItem>>> getWalletActivity(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                   @Query(Constants.DATE_FROM) String dateFrom,
                                                                   @Query(Constants.DATE_TO) String dateTo);

    @FormUrlEncoded
    @POST("sl-get-lists/")
    Call<GetShoppingListsApiResponse> getShoppingLists(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                       @Field(Constants.SYSTEM) String isSystemListAlsoNeeded);

    @FormUrlEncoded
    @POST("sl-edit-list/")
    Call<OldBaseApiResponse> editShoppingList(@Field(Constants.SLUG) String shoppingListSlug,
                                              @Field("name") String newName);

    @FormUrlEncoded
    @POST("sl-delete-list/")
    Call<OldBaseApiResponse> deleteShoppingList(@Field(Constants.SLUG) String shoppingListSlug);

    @FormUrlEncoded
    @POST("sl-create-list/")
    Call<OldBaseApiResponse> createShoppingList(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                @Field(Constants.SL_NAME) String shoppingListName,
                                                @Field(Constants.IS_PUBLIC) String isPublic);

    @GET("sl-get-list-summary/")
    Call<ApiResponse<GetShoppingListSummaryResponse>> getShoppingListSummary(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                             @Query(Constants.SLUG) String shoppingListSlug);

    @FormUrlEncoded
    @POST("sl-cat-items-to-cart/")
    Call<AddAllShoppingListItemResponse> addAllToBasketShoppingList(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                    @Field(Constants.SHOPPING_LIST_SLUG) String shoppingListSlug,
                                                                    @Field(Constants.CATEGORY_SLUG) String topCategorySlug);

    @FormUrlEncoded
    @POST("sb-cat-items-to-cart/")
    Call<AddAllShoppingListItemResponse> addAllToBasketSmartBasket(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                   @Field(Constants.SHOPPING_LIST_SLUG) String shoppingListSlug,
                                                                   @Field(Constants.CATEGORY_SLUG) String topCategorySlug);


    @FormUrlEncoded
    @POST("c-incr-i/")
    Call<CartOperationApiResponse> incrementCartItem(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                     @Field(TrackEventkeys.TERM) String searchTerm,
                                                     @Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                                                     @FieldMap Map<String, String> basketQueryMap);

    @FormUrlEncoded
    @POST("c-decr-i/")
    Call<CartOperationApiResponse> decrementCartItem(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                     @Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                                                     @FieldMap Map<String, String> basketQueryMap);

    @FormUrlEncoded
    @POST("c-set-i/")
    Call<CartOperationApiResponse> setCartItem(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                               @Field(TrackEventkeys.TERM) String searchTerm,
                                               @Field(Constants.PROD_ID) String productId, @Field(Constants.QTY) String qty,
                                               @FieldMap Map<String, String> basketQueryMap);

    @FormUrlEncoded
    @POST("change-password/")
    Call<OldBaseApiResponse> changePassword(@Field(Constants.OLD_PASSWORD) String oldPassword,
                                            @Field(Constants.NEW_PASSWORD) String newPassword,
                                            @Field(Constants.CONFIRM_PASSWORD) String confirmPassword);

    @GET("update-profile/")
    Call<ApiResponse<UpdateProfileApiResponse>> getMemberProfileData(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @FormUrlEncoded
    @POST("update-profile/")
    Call<ApiResponse<UpdateProfileApiResponse>> setUserDetailsData(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                   @Field(Constants.USER_DETAILS) String userDetails);

    @FormUrlEncoded
    @POST("login/")
    Call<ApiResponse<LoginApiResponse>> login(@Field(Constants.EMAIL) String email,
                                              @Field(Constants.PASSWORD) String password);

    @FormUrlEncoded
    @POST("social-login/")
    Call<ApiResponse<LoginApiResponse>> socialLogin(@Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType,
                                                    @Field(Constants.AUTH_TOKEN) String authToken);

    @FormUrlEncoded
    @POST("social-register-member/")
    Call<ApiResponse<LoginApiResponse>> socialRegisterMember(@Field(Constants.SOCIAL_LOGIN_TYPE) String socialLoginType,
                                                             @Field(Constants.AUTH_TOKEN) String authToken);

    @FormUrlEncoded
    @POST("register-member/")
    Call<ApiResponse<LoginApiResponse>> registerMember(@Field(Constants.USER_DETAILS) String userDetails);

    @FormUrlEncoded
    @POST("create-address/")
    Call<ApiResponse<CreateUpdateAddressApiResponseContent>> createAddress(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                           @FieldMap HashMap<String, String> params);


    @FormUrlEncoded
    @POST("update-address/")
    Call<ApiResponse<CreateUpdateAddressApiResponseContent>> updateAddress(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                           @FieldMap HashMap<String, String> params);

    @GET("get-orders/")
    Call<ApiResponse<OrderListApiResponse>> getOrders(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                      @Query(Constants.ORDER_TYPE) String orderType,
                                                      @Query(Constants.CURRENT_PAGE) String page);

    @GET("get-invoice/")
    Call<ApiResponse<OrderInvoice>> getInvoice(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                               @Query(Constants.ORDER_ID) String orderId);

    @FormUrlEncoded
    @POST("sl-add-item/")
    Call<OldBaseApiResponse> addItemToShoppingList(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                   @Field("product_id") String productId,
                                                   @Field("slugs") String shoppingListSlugs);

    @FormUrlEncoded
    @POST("sl-delete-item/")
    Call<OldBaseApiResponse> deleteItemFromShoppingList(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                        @Field("product_id") String productId,
                                                        @Field(Constants.SLUG) String shoppingListSlug);

    @GET("c-summary/")
    Call<CartSummaryApiResponse> cartSummary(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @GET("product-details/")
    Call<ProductDetailApiResponse> productDetails(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                  @Query(Constants.PROD_ID) String productId,
                                                  @Query(Constants.EAN_CODE) String eanCode);

    @GET("co-get-delivery-addresses/")
    Call<ApiResponse<GetDeliveryAddressApiResponseContent>> getDeliveryAddresses(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @GET("autosearch/")
    Call<ApiResponse<AutoSearchApiResponseContent>> autoSearch(@Query("t") String term, @Query(Constants.CITY_ID) String cityId);

    @FormUrlEncoded
    @POST("co-post-voucher/")
    Call<ApiResponse<PostVoucherApiResponseContent>> postVoucher(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                 @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                                 @Field(Constants.EVOUCHER_CODE) String evoucherCode);

    @GET("get-area-info/")
    Call<ApiResponse<GetAreaInfoResponse>> getAreaInfo(@Query(Constants.CITY_ID) String cityId);

    @FormUrlEncoded
    @POST("post-case-feedback/")
    Call<ApiResponse<PostFeedbackApiResponseContent>> postCaseFeedback(@Field(Constants.CASE_ID) String caseId,
                                                                       @Field(Constants.RATING) String rating,
                                                                       @Field(Constants.COMMENTS) String comments);

    @FormUrlEncoded
    @POST("update-version-number/")
    Call<ApiResponse<UpdateVersionInfoApiResponseContent>> updateVersionNumber(@Field(Constants.DEVICE_IMEI) String imei,
                                                                               @Field(Constants.DEVICE_ID) String deviceId,
                                                                               @Field(Constants.APP_VERSION) String appVersion);

    @GET("category-landing/")
    Call<ApiResponse<SubCategoryApiResponse>> getSubCategoryData(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                 @Query(Constants.CATEGORY_SLUG) String categorySlug,
                                                                 @Query(Constants.VERSION) String version);

    @GET("get-products-for-order/")
    Call<ApiResponse<GetProductsForOrderApiResponseContent>> getProductsForOrder(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                 @Query(Constants.ORDER_ID) String orderId);

    @FormUrlEncoded
    @POST("forgot-password/")
    Call<ApiResponse> updatePasswordWithOtp(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                            @Field(Constants.OTP_CODE) String otpCode,
                                            @Field(Constants.EMAIL) String email,
                                            @Field(Constants.NEW_PASSWORD) String newPassword);

    @GET("forgot-password/")
    Call<ApiResponse<OtpResponse>> getForgotPasswordOtp(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                        @Query(Constants.EMAIL) String email);

    @FormUrlEncoded
    @POST("remove-voucher/")
    Call<ApiResponse<PostVoucherApiResponseContent>> removeVoucher(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                   @Field(Constants.P_ORDER_ID) String potentialOrderId);

    @GET("get-app-data/")
    Call<ApiResponse<AppDataResponse>> getAppData(@Query(Constants.CLIENT) String client,
                                                  @Query(Constants.VERSION) String version);

    @FormUrlEncoded
    @POST("add-order-products/")
    Call<OldApiResponseWithCart> addAllToBasketPastOrders(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                          @Field(Constants.ORDER_ID) String orderId);

    @GET("get-order-payment-params/")
    Call<ApiResponse<PrePaymentParamsResponse>> getOrderPaymentParams(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                      @Query(Constants.P_ORDER_ID) String potentialOrderId);

    @GET("get-order-payment-params/")
    Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappOrderPaymentParams(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                    @Query(Constants.P_ORDER_ID) String potentialOrderId);

    @FormUrlEncoded
    @POST("validate-payment/")
    Call<ApiResponse<ValidateOrderPaymentApiResponse>> validatePayment(@Field(Constants.TXN_ID) String txnId,
                                                                       @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                                       @Field(Constants.ORDER_ID) String fullOrderId,
                                                                       @Field(Constants.PAYMENT_TYPE) String paymentType,
                                                                       @Field(Constants.PAY_NOW) String isPayNow,
                                                                       @Field(Constants.WALLET) String isFundWallet,
                                                                       @FieldMap Map<String, String> paymentParams);

    @GET("register-utm-params/")
    Call<BaseApiResponse> postUtmParams(@QueryMap Map<String, String> utmQueryMap);

    @GET("get-discount-page/")
    Call<ApiResponse<DiscountDataModel>> getDiscount(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx);

    @FormUrlEncoded
    @POST("co-create-po/")
    Call<ApiResponse<CreatePotentialOrderResponseContent>> createPotentialOrder(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                @Field(Constants.ADDRESS_ID) String addressId);

    @FormUrlEncoded
    @POST("co-post-gifts/")
    Call<ApiResponse<PostGiftItemsResponseContent>> postGifts(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                              @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                              @Field(Constants.GIFTS) String gifts);

    @FormUrlEncoded
    @POST("co-post-shipment/")
    Call<ApiResponse<PostShipmentResponseContent>> postShipment(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                @Field("shipments") String shipments,
                                                                @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                                @Field(Constants.SUPPORT_CC) String supportsCreditCard,
                                                                @Field(Constants.SUPPORT_POWER_PAY) String supportsPowerPay,
                                                                @Field(Constants.SUPPORT_MOBIKWIK) String supportsMobikWik,
                                                                @Field(Constants.SUPPORT_PAYTM) String supportsPaytm,
                                                                @Field(Constants.SUPPORT_PAYUMONEY) String supportsPayuMoney);

    @FormUrlEncoded
    @POST("co-place-order/")
    Call<OldApiResponse<PlaceOrderApiPrePaymentResponseContent>> placeOrderWithPrePayment(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                          @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                                                          @Field(Constants.PAYMENT_METHOD) String paymentMethod);

    @FormUrlEncoded
    @POST("co-place-order/")
    Call<OldApiResponse<PlaceOrderApiPayZappResponseContent>> placeOrderWithPayZapp(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                    @Field(Constants.P_ORDER_ID) String potentialOrderId,
                                                                                    @Field(Constants.PAYMENT_METHOD) String paymentMethod);


    //for v2.3.3 ,pay-now irrespective of the payment type, key to send order id is 'order_ids'
    @GET("pay-now/")
    Call<ApiResponse<GetPayNowParamsResponse>> getPayNowDetails(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                @Query(Constants.ORDER_IDS) String orderId,
                                                                @Query(Constants.SUPPORT_POWER_PAY) String supportPp,
                                                                @Query(Constants.SUPPORT_CC) String supportPayu,
                                                                @Query(Constants.SUPPORT_MOBIKWIK) String mobikWik,
                                                                @Query(Constants.SUPPORT_PAYTM) String supportsPaytm,
                                                                @Query(Constants.SUPPORT_PAYUMONEY) String supportsPayuMoney);

    @FormUrlEncoded
    @POST("pay-now/")
    Call<ApiResponse<PrePaymentParamsResponse>> postPayNowDetails(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                  @Field(Constants.ORDER_IDS) String orderId,
                                                                  @Field(Constants.PAYMENT_METHOD) String paymentMethod);

    @FormUrlEncoded
    @POST("pay-now/")
    Call<ApiResponse<PayzappPrePaymentParamsResponse>> postPayzappPayNowDetails(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                                @Field(Constants.ORDER_IDS) String orderId,
                                                                                @Field(Constants.PAYMENT_METHOD) String paymentMethod);

    @GET("fund-wallet/")
    Call<ApiResponse<GetPaymentTypes>> getFundWalletPayments(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                             @Query(Constants.SUPPORT_CC) String supportsPayu,
                                                             @Query(Constants.SUPPORT_POWER_PAY) String supportPowerPay,
                                                             @Query(Constants.SUPPORT_MOBIKWIK) String mobikwik,
                                                             @Query(Constants.SUPPORT_PAYTM) String supportsPaytm,
                                                             @Query(Constants.SUPPORT_PAYUMONEY) String supportsPayuMoney);

    @FormUrlEncoded
    @POST("fund-wallet/")
    Call<ApiResponse<PrePaymentParamsResponse>> postFundWallet(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                               @Field(Constants.PAYMENT_METHOD) String payment_method,
                                                               @Field(Constants.AMOUNT) String amount);

    @FormUrlEncoded
    @POST("fund-wallet/")
    Call<ApiResponse<PayzappPrePaymentParamsResponse>> postPayzappFundWallet(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                             @Field(Constants.PAYMENT_METHOD) String payment_method,
                                                                             @Field(Constants.AMOUNT) String amount);

    @FormUrlEncoded
    @POST("set-current-address/")
    Call<ApiResponse<SetAddressResponse>> setCurrentAddress(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                            @Field(Constants.ID) String id,
                                                            @Field(Constants.LAT) String latitude,
                                                            @Field(Constants.LNG) String longitude,
                                                            @Field(Constants.AREA) String area);

    @FormUrlEncoded
    @POST("set-current-address/")
    Call<ApiResponse<SetAddressTransientResponse>> setCurrentAddress(@Field(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                                     @Field(Constants.ID) String id,
                                                                     @Field(Constants.LAT) String latitude,
                                                                     @Field(Constants.LNG) String longitude,
                                                                     @Field(Constants.TRANSIENT) String isTransient,
                                                                     @Field(Constants.AREA) String area);

    @GET("get-location-detail/")
    Call<ApiResponse<AddressSummary>> getLocationDetail(@Query(TrackEventkeys.NAVIGATION_CTX) String navigationCtx,
                                                        @Query(Constants.LAT) String latitude,
                                                        @Query(Constants.LNG) String longitude);

    @POST("logout/")
    Call<BaseApiResponse> logout();

}
