package com.bigbasket.mobileapp.interfaces;

import java.util.Map;

public interface TrackingAware {


    //Start Activity
    public static final String ENTRY_PAGE_SHOWN = "EntryPage.Shown";
    public static final String ENTRY_PAGE_START_SHOPPING_BTN_CLICKED = "EntryPage.Start Shopping Clicked";

    //Home Screen topnav
    public static final String COMMUNICATION_HUB_CLICKED = "MyAccount.CommunicationHubClicked";
    public static final String LOGIN_CLICKED = "Login Clicked";
    public static final String REGISTRATION_CLICKED = "Sign up Clicked";
    public static final String HOME_CHANGE_CITY = "Change Your Location Clicked";
    public static final String RATE_APP_CLICKED = "Rate This App Clicked";
    public static final String MENU_CLICKED = "Menu.Clicked";

    //change city dialog
    public static final String CHANGE_CITY_POSSITIVE_BTN_CLICKED = "Change Your City. Change City Clicked";
    public static final String CHANGE_CITY_CANCEL_BTN_CLICKED = "Change Your City.Cancel Clicked";

    //login and register
    public static final String LOGIN_SHOWN = "Login.Login Page Shown";
    public static final String LOGIN_BTN_CLICKED = "Login.Login Button Clicked";
    public static final String LOGIN_FAILED = "Login.Login Failed";
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
    public static final String MY_ACCOUNT_CLICKED = "My Account.My Account Clicked";
    public static final String MY_ACCOUNT_SHOWN = "MyAccount.My Account Shown";
    public static final String MY_ACCOUNT_ACTIVE_ORDER_CLICKED = "MyAccount.Active Orders Clicked";
    public static final String MY_ACCOUNT_PAST_ORDER_CLICKED = "MyAccount.View Order History Clicked";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_CLICKED = "MyAccount.Update Profile Clicked";


    //update_profile
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

    //shopping list
    public static final String SHOP_LST_SHOWN = "ShoppingList.All Lists Shown";
    public static final String SHOP_LST_CREATED = "ShoppingList.Created";
    public static final String SHOP_LST_NAME_CHANGED = "ShoppingList.Name Changed";
    public static final String SHOP_LST_DELETED = "ShoppingList.Deleted";
    public static final String SHOP_LST_SUMMARY_SHOWN = "ShoppingList.Summary Shown";


    //main menu
    public static final String MENU_SHOWN = "Menu.Shown";
    public static final String MENU_ITEM_CLICKED = "Menu.Item Clicked";

    //Home Page
    public static final String HOME_PAGE_SHOWN = "HomePage.Shown";
    public static final String DYNAMIC_SCREEN_SHOWN = "Dynamic Screen.Shown";
    public static final String HOME_PAGE_ITEM_CLICKED = "HomePage.Item Clicked";
    public static final String HOME_PAGE_BANNER_CLICKED = "HomePage.Banner Clicked";
    public static final String SEARCH = "Search";

    //product listing page
    public static final String PRODUCT_LIST_SHOWN = "ProductListing Shown";
    public static final String PRODUCT_DETAIL_SHOWN = "ProductDetail Shown";

    //promo
    public static final String PROMO_DETAIL_SHOWN = "Promo.Promo Detail Shown";
    public static final String PROMO_SET_PRODUCTS_SHOWN = "Promo.Promo Set Products Shown";
    public static final String PROMO_CATEGORY_LIST = "Promo.Category List";
    public static final String PROMO_REDEEMED = "Promo.Promo Redeemed";


    //basket
    public static final String BASKET_INCREMENT = "Basket.Increment Clicked";
    public static final String BASKET_ADD = "Basket.add Clicked";
    public static final String BASKET_DECREMENT = "Basket.Decrement Clicked";
    public static final String BASKET_REMOVE = "Basket.Remove Clicked";
    public static final String BASKET_EMPTY_CLICKED = "Basket.Empty Clicked";
    public static final String BASKET_VIEW_CLICKED = "Basket.View Clicked";

    //subCat
    public static final String CATEGORY_LANDING_SHOWN = "Category.Landing Shown";

    //smart and shopping list Basket
    public static final String SMART_BASKET = "SmartBasket";
    public static final String SHOPPING_LIST = "ShoppingList";
    public static final String SMART_BASKET_SUMMARY_SHOWN = "SmartBasket.Summary Shown";
    public static final String ADD_TO_SHOPPING_LIST = "Add To ShoppingList";

    //product page dialog
    public static final String FILTER_APPLIED = "Filter.Applied";
    public static final String SORT_BY = "SortBy.Selected";

    //precheckout
    public static final String PRE_CHECKOUT_AGE_LEGAL_SHOWN = "Precheckout.Age Legal Shown";
    public static final String PRE_CHECKOUT_AGE_LEGAL_ACCEPTED = "Precheckout.Age Legal Accepted";
    public static final String PRE_CHECKOUT_AGE_LEGAL_REJECTED = "Precheckout.Age Legal Rejected";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_CHOSEN = "Precheckout.Pharma Prescription Chosen";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_CREATED = "Precheckout.Pharma Prescription Created";
    public static final String PRE_CHECKOUT_PHARMA_PRESCRIPTION_NOT_PROVIDED = "Precheckout.Pharma Prescription Not Provided";
    public static final String PRE_CHECKOUT_CWR_APPICABLE = "Precheckout.Category Weight Restriction Applicable";

    //check out
    public static final String CHECKOUT_QC_SHOWN = "Checkout.Order QC Shown";
    public static final String CHECK_OUT_QC_PROCEED_BTN = "Checkout.QC Proceed Clicked";
    public static final String CHECK_CREATE_ADDRESS_SHOWN = "Checkout.Address Shown";
    public static final String CHECKOUT_ADDRESS_CREATED = "Checkout.Address Created";
    public static final String CHECKOUT_SLOT_SHOWN = "Checkout.Slot Shown";
    public static final String CHECKOUT_SLOT_CLICKED = "Checkout.Slot Clicked";
    public static final String CHECKOUT_PAYMENT_SHOWN = "Checkout.Payment Shown";
    public static final String CHECKOUT_ORDER_REVIEW_CLICKED = "Checkout.Order Review Clicked";
    public static final String CHECKOUT_VOUCHER_APPLIED = "Checkout.Voucher Applied";
    public static final String CHECKOUT_VOUCHER_FAILED = "Checkout.Voucher Failed";
    public static final String CHECKOUT_ORDER_REVIEW_SHOWN = "Checkout.Order Review Shown";
    public static final String CHECKOUT_PAYMENT_GATEWAY_SUCCESS = "Checkout.Payment Gateway Success";
    public static final String CHECKOUT_PAYMENT_GATEWAY_FAILURE = "Checkout.Payment Gateway Failure";// todo failure reason
    public static final String CHECKOUT_PLACE_ORDER_CLICKED = "Checkout.Place Order Clicked";
    public static final String CHECKOUT_PLACE_ORDER_AMOUNT_MISMATCH = "Checkout.Place Order Amount Mismatch";
    public static final String CHECKOUT_ORDER_COMPLETE = "Checkout.Order Complete";

    //thank you
    public static final String THANK_YOU_PAGE_SHOWN = "Checkout.Thank You Page Shown";
    public static final String THANK_YOU_VIEW_INVOICE_CLICKED = "Checkout.View Invoice Clicked";

    public static final String BASKET_ADD_PROMO_BUNDLE = "Basket.Add Promo Bundle"; //todo


    //member referral
    public static final String MEMBER_REFERRAL_SHOWN = "Member.Referral Shown";
    public static final String MEMBER_REFERRAL_FREE_SMS_SHOWN = "Member.Referral free SMS Shown";
    public static final String MEMBER_REFERRAL_WHATS_APP_SHOWN = "Member.Referral WhatsApp Shown";
    public static final String MEMBER_REFERRAL_FACEBOOK_SHOWN = "Member.Referral Facebook Shown";
    public static final String MEMBER_REFERRAL_BB_MAIL_SHOWN = "Member.Referral Mail Shown";
    public static final String MEMBER_REFERRAL_GOOGLE_PLUS_SHOWN = "Member.Referral Google Plus Shown";
    public static final String MEMBER_REFERRAL_GOOGLE_APP_SHOWN = "Member.Referral Google App Shown";
    public static final String MEMBER_REFERRAL_HIKE_SHOWN = "Member.Referral Hike Shown";
    public static final String MEMBER_REFERRAL_OTHER_SHOWN = "Member.Referral Shear via other Shown";


    public void trackEvent(String eventName, Map<String, String> eventAttribs,
                           String source, String sourceValue, boolean isCustomerValueIncrease);

    public void trackEvent(String eventName, Map<String, String> eventAttribs);
}
