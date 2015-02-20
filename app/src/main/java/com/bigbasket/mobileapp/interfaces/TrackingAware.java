package com.bigbasket.mobileapp.interfaces;

import java.util.Map;

public interface TrackingAware {


    //Start Activity
    public static final String ENTRY_PAGE_SHOWN = "EntryPage.Shown";
    public static final String ENTRY_PAGE_SPINNER_CLICKED = "EntryPage.City Selection Clicked";
    public static final String ENTRY_PAGE_START_SHOPPING_BTN_CLICKED ="EntryPage.Start Shopping Clicked";

    //Home Screen
    public static final String COMMUNICATION_HUB_CLICKED = "MyAccount.CommunicationHubClicked";
    public static final String LOGIN_OR_REGISTRATION_CLICKED = "Login or Register Clicked";
    public static final String HOME_CHANGE_CITY = "Change Your Location Clicked";
    public static final String RATE_APP_CLICKED = "Rate This App Clicked";
    public static final String MENU_CLICKED = "Menu.Clicked";

    //change city dialog
    public static final String CHANGE_CITY_DIALOG_SHOWN = "Change Your City. Dialog Shown";
    public static final String CHANGE_CITY_POSSITIVE_BTN_CLICKED = "Change Your City. Change City Clicked";
    public static final String CHANGE_CITY_CANCEL_BTN_CLICKED = "Change Your City.Cancel Clicked";

    //login and register
    public static final String LOGIN_SHOWN = "Login.Login Page Shown";
    public static final String LOGIN_BTN_CLICKED = "Login.Login Button Clicked";
    public static final String LOGIN_FAILED ="Login.Login Failed";
    public static final String SHOW_PASSWORD_ENABLED = "Show Password Enabled";
    public static final String FORGOT_PASSWORD_CLICKED = "Login.Forgot PassWord Clicked";
    public static final String NEW_USER_REGISTER_CLICKED = "Login.New User Register Clicked";
    public static final String FORGOT_PASSWORD_DIALOG_SHOWN = "Forgot Password.Dialog Shown";
    public static final String FORGOT_PASSWORD_EMAIL_CLICKED = "Forgot Password.Email New Password Clicked";
    public static final String REGISTRATION_PAGE_SHOWN = "Registration Page.Shown";
    public static final String REGISTRATION_PAGE_TC_CLICKED = "Registration Page.Terms And Conditions Clicked";
    public static final String PROMO_MAILER_ENABLED = "Promotional Mailer Enabled";
    public static final String REGISTER_BTN_CLICK = "Registration Page.Register Clicked";
    public static final String REGISTRATION_FAILED = "Registration Page. Failed";


    //Account
    public static final String MYACCOUNT_CLICKED = "My Account.My Account Clicked";
    public static final String MY_ACCOUNT_SHOWN = "MyAccount.My Account Shown";
    public static final String MY_ACCOUNT_ACTIVE_ORDER_CLICKED = "MyAccount.Active Orders Clicked";
    public static final String MY_ACCOUNT_PAST_ORDER_CLICKED = "MyAccount.View Order History Clicked";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_CLICKED = "MyAccount.Update Profile Clicked";


    //update_profile
    public static final String UPDATE_PROFILE_CLICKED = "MyAccount.Profile Update Clicked";
    public static final String UPDATE_PROFILE_SHOWN = "MyAccount.Profile Shown";
    public static final String UPDATE_PROFILE_GET_FAILED = "MyAccount.Update Profile Failed";
    public static final String UPDATE_PROFILE_SUBMIT_BTN_CLICKED = "MyAccount.Profile Update Clicked";
    //otp dialog
    public static final String OTP_DIALOG_SHOWN = "MyAccount.OTPDialog Shown";
    public static final String OTP_SUBMIT_BTN_CLICKED = "MyAccount.OTP Submit Clicked";

    //change password
    public static final String CHANGE_PASSWORD_CLICKED = "MyAccount.Change Password Clicked";
    public static final String CHANGE_PASSWORD_SHOWN = "MyAccount.Change Password Screen Shown";
    public static final String CHANGE_PASSWORD_FAILED = "MyAccount.Change Password Update Clicked";

    //wallet
    public static final String MY_ACCOUNT_WALLET_CLICKED = "MyAccount.Wallet Activity Clicked";
    public static final String WALLET_SUMMARY_SHOWN = "MyAccount.Wallet Summary Shown";
    public static final String WALLET_ACTIVITY_FOR_MONTH_CLICKED = "MyAccount.Wallet Activity For Month Clicked";

    //Address
    public static final String DELIVERY_ADDRESS_CLICKED = "Delivery Addresses Clicked";
    public static final String DELIVERY_ADDRESS_SHOWN = "Delivery Addresses Shown";
    public static final String ADDRESS_CLICKED = "Address Clicked";
    public static final String NEW_ADDRESS_CLICKED = "Address New Clicked";
    public static final String ENABLE_DEFAULT_ADDRESS = "Make This Default Address Enabled";
    public static final String NEW_ADDRESS_FAILED = "New Address Add Failed";
    public static final String UPDATE_ADDRESS_FAILED = "Update Address Failed";

    //delivery pin
    public static final String CHANGE_PIN_CLICKED = "MyAccount.Change Pin Clicked";
    public static final String CHANGE_PIN_SHOWN = "MyAccount.Change Pin Page Shown";
    public static final String UPDATE_PIN_CLICKED = "MyAccount.Change Pin Update Clicked";

    //
    public static final String SPEND_TRENDS_CLICKED = "MyAccount.Spend Trends Clicked";
    public static final String SPEND_TRENDS_SHOWN = "MyAccount.Spend Trends Shown";



    //topnav and home page
    public static final String SHOPPING_LIST_ICON_CLICKED = "Shopping Lists Icon Clicked";
    public static final String SMART_BASKET_ICON_CLICKED = "Smart Basket Icon Clicked";

    //logout
    public static final String LOG_OUT_ICON_CLICKED = "Logout Icon Clicked";

    //active and past order
    public static final String ORDER_ACTIVE_ORDERS_SHOWN = "Active Orders Shown";
    public static final String ORDER_PAST_ORDERS_SHOWN = "Order History List Shown";
    public static final String ORDER_SUMMARY_SHOWN = "Order.Order Details Shown";
    public static final String ORDER_ITEMS_TAB_CLICKED = "Order.View Items Clicked";











    public static final String BASKET_INCREMENT = "Basket.Increment";
    public static final String BASKET_ADD = "Basket.Add";
    public static final String BASKET_DECREMENT = "Basket.Decrement";
    public static final String BASKET_REMOVE = "Basket.Remove";
    public static final String BASKET_ADD_PROMO_BUNDLE = "Basket.Add Promo Bundle"; //todo
    public static final String BASKET_EMPTY = "Basket.Empty";
    public static final String BASKET_VIEW = "Basket.View";

    public static final String BROWSE_CATEGORY_LANDING = "Browse.Category Landing";
    public static final String BROWSE_PRODUCT_CATEGORY = "Browse.Product Category";
    public static final String BROWSE_DISCOUNTS = "Browse.Discounts";
    public static final String BROWSE_PRODUCT_DETAILS = "Browse.Product Details";
    public static final String SEARCH = "Search";

    public static final String PRE_CHECKOUT_AGE_LEGAL_SHOWN = "Precheckout.Age Legal Shown";
    public static final String PRE_CHECKOUT_AGE_LEGAL_ACCEPTED = "Precheckout.Age Legal Accepted";
    public static final String PRE_CHECKOUT_AGE_LEGAL_REJECTED = "Precheckout.Age Legal Rejected";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_CHOSEN = "Precheckout.Pharma Prescription Chosen";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_CREATED = "Precheckout.Pharma Prescription Created";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_NOT_PROVIDED = "Precheckout.Pharma Prescription Not Provided";
    public static final String PRE_CHECKOUT_CWR_APPICABLE = "Precheckout.Category Weight Restriction Applicable";

    public static final String CHECKOUT_QC_SHOWN = "Checkout.Order QC Shown";

    public static final String CHECKOUT_ADDRESS_SHOWN = "Checkout.Address Shown";
    public static final String CHECKOUT_ADDRESS_CHOSEN = "Checkout.Address Choosen";
    public static final String CHECKOUT_ADDRESS_CREATED = "Checkout.Address Created";

    public static final String CHECKOUT_SLOT_SHOWN = "Checkout.Slot Shown";
    public static final String CHECKOUT_SLOT_CHOOSEN = "Checkout.Slot Choosen";

    public static final String CHECKOUT_PAYMENT_SHOWN = "Checkout.Payment Shown";
    public static final String CHECKOUT_PAYMENT_CHOSEN = "Checkout.Payment Chosen";
    public static final String CHECKOUT_VOUCHER_APPLIED = "Checkout.Voucher Applied";
    public static final String CHECKOUT_VOUCHER_FAILED = "Checkout.Voucher Failed";

    public static final String CHECKOUT_ORDER_REVIEW_SHOWN = "Checkout.Order Review Shown";
    public static final String CHECKOUT_PAYMENT_GATEWAY_SUCCESS = "Checkout.Payment Gateway Success";
    public static final String CHECKOUT_PAYMENT_GATEWAY_FAILURE = "Checkout.Payment Gateway Failure";
    public static final String CHECKOUT_PLACE_ORDER_CLICKED = "Checkout.Place Order Clicked";
    public static final String CHECKOUT_PLACE_ORDER_AMOUNT_MISMATCH = "Checkout.Place Order Amount Mismatch";
    public static final String ORDER_VIEW_INVOICE = "Order.View Invoice";
    public static final String CHECKOUT_ORDER_COMPLETE = "Checkout.Order Complete";


    public static final String FORGOT_PASSWORD_PWD_SUCCESS = "MyAccount.ForgotPassword Success";

    public static final String NOW_AT_BB = "Browse.NowAtBigBasketShown";
    public static final String NEW_AT_BB = "Browse.NewAtBigBasketShown";
    public static final String BUNDLE_PACK = "Browse.BundlePackShown";

    public static final String FILTER_APPLIED = "Filter.Applied";

    public static final String HOME_CITY_SELECTION = "HomePage.CitySelection";



    public static final String MY_ACCOUNT_CHANGE_PASSWD_SELECTED = "MyAccount.Change Password Selected";
    public static final String MY_ACCOUNT_CHANGE_PASSWD_SUCCESS = "MyAccount.Change Password Success";
    public static final String MY_ACCOUNT_CHANGE_PASSWD_FAILED = "MyAccount.Change Password Failed";


    public static final String MY_ACCOUNT_UPDATE_PROFILE_SELECTED = "MyAccount.Update Profile Selected";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_SUCCESS = "MyAccount.Update Profile Success";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_FAILED = "MyAccount.Update Profile Failed";


    public static final String MY_ACCOUNT_CURRENT_PIN_SUCCESS = "MyAccount.Change Current Pin Success";
    public static final String MY_ACCOUNT_CURRENT_PIN_FAILED = "MyAccount.Change Current Pin Failed";
    public static final String MY_ACCOUNT_CHANGE_PIN_SUCCESS = "MyAccount.Change Update Pin Success";
    public static final String MY_ACCOUNT_CHANGE_PIN_FAILED = "MyAccount.Change Update Pin Failed";

    public static final String MY_ACCOUNT_CURRENT_WALLET_BALANCE_SUCCESS = "MyAccount.Wallet Current Wallet Success";
    public static final String MY_ACCOUNT_CURRENT_WALLET_BALANCE_FAILED = "MyAccount.Wallet Current Wallet Failed";
    public static final String MY_ACCOUNT_WALLET_ACTIVITY_SUCCESS = "MyAccount.Wallet Activity Success";
    public static final String MY_ACCOUNT_WALLET_ACTIVITY_FAILED = "MyAccount.Wallet Activity  Failed";

    public static final String MY_ACCOUNT_DELIVERY_ADDRESS_SHOWN = "MyAccount.Delivery Addresses Shown";
    public static final String MY_ACCOUNT_ADDRESS_EDITED = "MyAccount.Address Edited";
    public static final String MY_ACCOUNT_ADDRESS_CREATED = "MyAccount.Address Created";

    public static final String MY_ACCOUNT_LOGIN_SUCCESS = "MyAccount.Login Success";
    public static final String MY_ACCOUNT_LOGIN_FAILED = "MyAccount.Login Failed";

    public static final String MY_ACCOUNT_FACEBOOK_LOGIN = "MyAccount.Facebook Login"; //onFacebookSignIn()
    public static final String MY_ACCOUNT_FACEBOOK_LOGIN_SUCCESS = "MyAccount.Facebook Login Success"; //on api call retrofit BaseSignInSignupActivity
    public static final String MY_ACCOUNT_GOOGLE_LOGIN_SUCCESS = "MyAccount.Google Login Success";      //on api call retrofit
    public static final String MY_ACCOUNT_FACEBOOK_LOGIN_FAILED = "MyAccount.Facebook Login Failed";    //on api call retrofit
    public static final String MY_ACCOUNT_GOOGLE_LOGIN_FAILED = "MyAccount.Google Login Failed";        //on api call retrofit

    public static final String MY_ACCOUNT_LOGOUT = "MyAccount.Logout";                                  //BaseActivity onLogoutRequested()
    public static final String MY_ACCOUNT_FACEBOOK_LOGOUT = "MyAccount.Facebook Account Logout";        // onFacebookSignOut()
    public static final String MY_ACCOUNT_GOOGLE_LOGOUT = "MyAccount.Google Account Logout";            //onPlusClientSignOut()

    public static final String MY_ACCOUNT_REGISTRATION_SHOWN = "MyAccount.Registration Shown";
    public static final String MY_ACCOUNT_REGISTRATION_SUCCESS = "MyAccount.Registration Success";
    public static final String MY_ACCOUNT_REGISTRATION_FAILED = "MyAccount.Registration Failed";


    public static final String PROMO_CATEGORY_LIST = "Promo.Category List";
    public static final String PROMO_DETAIL = "Promo.Promo Detail";
    public static final String PROMO_SET_PRODUCTS_SHOWN = "Promo.Promo Set Products Shown";
    public static final String PROMO_REDEEMED = "Promo.Promo Redeemed";

    public static final String SHOP_LST_SHOWN = "ShoppingList.All Lists Shown";
    public static final String SHOP_LST_SUMMARY_SHOWN = "ShoppingList.Summary Shown";
    public static final String SHOP_LST_SYSTEM_LST_SHOWN = "ShoppingList.System ShoppingList.Summary Shown";
    public static final String SHOP_LST_CATEGORY_DETAIL = "ShoppingList.Category Details";
    public static final String SHOP_LST_SYSTEM_LIST_CATEGORY_DETAIL = "ShoppingList.System ShoppingList.Category Details";
    public static final String SHOP_LST_NAME_CHANGED = "ShoppingList.Name Changed";
    public static final String SHOP_LST_DELETED = "ShoppingList.List Deleted";
    public static final String SHOP_LST_CREATED = "ShoppingList.List Created";
    public static final String SHOP_LIST_PRODUCT_ADDED = "ShoppingList.Product Added";
    public static final String SHOP_LIST_PRODUCT_DELETED = "ShoppingList.Product Deleted";

    public static final String SHOP_FROM_PAST_ORDER_SHOWN = "Order.Shop from Past Order";

    public static final String MEMBER_REFERRAL_SHOWN = "Member.Referral Shown";
    public static final String MEMBER_REFERRAL_FREE_SMS_SHOWN = "Member.Referral free SMS Shown";
    public static final String MEMBER_REFERRAL_WHATS_APP_SHOWN = "Member.Referral WhatsApp Shown";
    public static final String MEMBER_REFERRAL_FACEBOOK_SHOWN = "Member.Referral Facebook Shown";
    public static final String MEMBER_REFERRAL_BB_MAIL_SHOWN = "Member.Referral Mail Shown";
    public static final String MEMBER_REFERRAL_GOOGLE_PLUS_SHOWN = "Member.Referral Google Plus Shown";
    public static final String MEMBER_REFERRAL_GOOGLE_APP_SHOWN = "Member.Referral Google App Shown";
    public static final String MEMBER_REFERRAL_HIKE_SHOWN = "Member.Referral Hike Shown";
    public static final String MEMBER_REFERRAL_OTHER_SHOWN = "Member.Referral Shear via other Shown";

    public static final String SPENDTRENDS_SHOWN = "SpendTrends Shown";

    public void trackEvent(String eventName, Map<String, String> eventAttribs,
                           String source, String sourceValue, boolean isCustomerValueIncrease);

    public void trackEvent(String eventName, Map<String, String> eventAttribs);
}
