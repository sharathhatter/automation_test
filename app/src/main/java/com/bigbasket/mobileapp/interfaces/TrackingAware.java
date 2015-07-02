package com.bigbasket.mobileapp.interfaces;

import java.util.Map;

public interface TrackingAware {


    //Start Activity
    String ENTRY_PAGE_SHOWN = "EntryPage.Shown";
    String ENTRY_PAGE_SKIP_BUTTON_CLICKED = "EntryPage.Skip Button Clicked";
    String ENTRY_PAGE_LOGIN_CLICKED = "EntryPage.Login Button Clicked";
    String ENTRY_PAGE_SIGNUP_CLICKED = "EntryPage.Signup Button Clicked";

    //Home Screen topnav
    String COMMUNICATION_HUB_CLICKED = "MyAccount.CommunicationHubClicked";
    String LOGIN_CLICKED = "Login Clicked";
    String REGISTRATION_CLICKED = "Sign up Clicked";
    String HOME_CHANGE_CITY = "Change Your Location Clicked";
    String RATE_APP_CLICKED = "Rate This App Clicked";
    String MENU_CLICKED = "Menu.Clicked";

    //change city dialog
    String CHANGE_CITY_POSSITIVE_BTN_CLICKED = "Change Your City. Change City Clicked";
    String CHANGE_CITY_SHOWN = "Change Your City.Shown";
    String CHANGE_CITY_CLICKED = "Change Your City.Change City Clicked";

    //login and register
    String LOGIN_SHOWN = "Login.Login Page Shown";
    String LOGIN_BTN_CLICKED = "Login.Login Button Clicked";
    String LOGIN_FAILED = "Login.Login Failed";
    String SHOW_PASSWORD_ENABLED = "Show Password Enabled";
    String LOGIN_REMEMBER_ME_ENABLED = "Login.Remember Me Enabled";
    String FORGOT_PASSWORD_CLICKED = "Login.Forgot PassWord Clicked";
    String NEW_USER_REGISTER_CLICKED = "Login.New User Register Clicked";
    String FORGOT_PASSWORD_DIALOG_SHOWN = "Forgot Password.Dialog Shown";
    String FORGOT_PASSWORD_EMAIL_CLICKED = "Forgot Password.Email New Password Clicked";
    String REGISTRATION_PAGE_SHOWN = "Registration Page.Shown";
    String PROMO_MAILER_ENABLED = "Promotional Mailer Enabled";
    String REGISTER_BTN_CLICK = "Registration Page.Register Clicked";
    String REGISTRATION_FAILED = "Registration Page. Failed";


    //Account
    String MY_ACCOUNT_CLICKED = "My Account.My Account Clicked";
    String MY_ACCOUNT_SHOWN = "MyAccount.My Account Shown";
    String MY_ORDER_CLICKED = "My Orders Clicked";
    String MY_ORDER_SHOWN = "My Orders Shown";
    String MY_ORDER_ITEM_CLICKED = "My Orders Item Clicked";
    String MY_ACCOUNT_UPDATE_PROFILE_CLICKED = "MyAccount.Update Profile Clicked";


    //update_profile
    String UPDATE_PROFILE_SHOWN = "MyAccount.Profile Shown";
    String UPDATE_PROFILE_GET_FAILED = "MyAccount.Update Profile Failed";
    String UPDATE_PROFILE_SUBMIT_BTN_CLICKED = "MyAccount.Profile Update Clicked";
    //otp dialog
    String OTP_DIALOG_SHOWN = "MyAccount.OTPDialog Shown";
    String OTP_SUBMIT_BTN_CLICKED = "MyAccount.OTP Submit Clicked";

    //change password
    String CHANGE_PASSWORD_CLICKED = "MyAccount.Change Password Clicked";
    String CHANGE_PASSWORD_SHOWN = "MyAccount.Change Password Screen Shown";
    String CHANGE_PASSWORD_FAILED = "MyAccount.Change Password Update Clicked";

    //wallet
    String MY_ACCOUNT_WALLET_CLICKED = "MyAccount.Wallet Activity Clicked";
    String WALLET_SUMMARY_SHOWN = "MyAccount.Wallet Summary Shown";
    String WALLET_ACTIVITY_FOR_MONTH_CLICKED = "MyAccount.Wallet Activity For Month Clicked";

    //Address
    String DELIVERY_ADDRESS_CLICKED = "Delivery Addresses Clicked";
    String DELIVERY_ADDRESS_SHOWN = "Delivery Addresses Shown";
    String NEW_ADDRESS_CLICKED = "Address New Clicked";
    String ENABLE_DEFAULT_ADDRESS = "Make This Default Address Enabled";
    String NEW_ADDRESS_FAILED = "New Address Add Failed";
    String UPDATE_ADDRESS_FAILED = "Update Address Failed";

    //delivery pin
    String CHANGE_PIN_CLICKED = "MyAccount.Change Pin Clicked";
    String CHANGE_PIN_SHOWN = "MyAccount.Change Pin Page Shown";
    String UPDATE_PIN_CLICKED = "MyAccount.Change Pin Update Clicked";

    //topnav and home page
    String SHOPPING_LIST_ICON_CLICKED = "Shopping Lists Icon Clicked";
    String SMART_BASKET_ICON_CLICKED = "Smart Basket Icon Clicked";

    //logout
    String LOG_OUT_ICON_CLICKED = "Logout Icon Clicked";

    //active and past order
    String ORDER_SUMMARY_SHOWN = "Order.Order Details Shown";
    String ORDER_ITEMS_TAB_CLICKED = "Order.View Items Clicked";
    String SHOP_FROM_PAST_ORDER = "Shop from This Order";

    //shopping list
    String SHOP_LST_SHOWN = "ShoppingList.All Lists Shown";
    String SHOP_LST_CREATED = "ShoppingList.Created";
    String SHOP_LST_NAME_CHANGED = "ShoppingList.Name Changed";
    String SHOP_LST_DELETED = "ShoppingList.Deleted";
    String SHOP_LST_SUMMARY_SHOWN = "ShoppingList.Summary Shown";


    //main menu
    String MENU_SHOWN = "Menu.Shown";
    String MENU_ITEM_CLICKED = "Menu.Item Clicked";

    // Dynamic Page
    String ITEM_CLICKED = "Item Clicked";

    //Home Page
    String HOME_PAGE_SHOWN = "HomePage.Shown";
    String DYNAMIC_SCREEN_SHOWN = "Dynamic Screen.Shown";
    String HOME_PAGE_ITEM_CLICKED = "HomePage.Item Clicked";
    String SEARCH = "Search";

    //product listing page
    String PRODUCT_LIST_SHOWN = "Product Listing.Shown";
    String PRODUCT_LIST_TAB_CHANGED = "Product List.Tab Changed";
    String PRODUCT_LIST_HEADER = "Product List Header";
    String PRODUCT_DETAIL_SHOWN = "Product Detail Shown";
    String PRODUCT_LIST_HEADER_CLICKED = "Product Listing.Header Clicked";

    //promo
    String PROMO_DETAIL_SHOWN = "Promo.Promo Detail Shown";
    String PROMO_SET_PRODUCTS_SHOWN = "Promo.Promo Set Products Shown";
    String PROMO_CATEGORY_LIST = "Promo.Category List";
    String PROMO_REDEEMED = "Promo.Promo Redeemed";


    //basket
    String BASKET_INCREMENT = "Basket.Increment Clicked";
    String BASKET_ADD = "Basket.add Clicked";
    String BASKET_DECREMENT = "Basket.Decrement Clicked";
    String BASKET_REMOVE = "Basket.Remove Clicked";
    String BASKET_EMPTY_CLICKED = "Basket.Empty Clicked";
    String BASKET_VIEW_CLICKED = "Basket.View Clicked";

    //subCat
    String CATEGORY_LANDING_SHOWN = "Category.Landing Shown";

    //smart and shopping list Basket
    String SMART_BASKET = "SmartBasket";
    String SHOPPING_LIST = "ShoppingList";
    String SMART_BASKET_SUMMARY_SHOWN = "SmartBasket.Summary Shown";
    String ADD_TO_SHOPPING_LIST = "Add To ShoppingList";

    //product page dialog
    String FILTER_APPLIED = "Filter.Applied";
    String FILTER_CLEARED = "Filter.Cleared";
    String SORT_BY = "SortBy.Selected";

    //check out
    String CHECKOUT_QC_SHOWN = "Checkout.Order QC Shown";
    String CHECKOUT_CREATE_ADDRESS_SHOWN = "Checkout.Address Shown";
    String CHECKOUT_ADDRESS_CREATED = "Checkout.Address Created";
    String CHECKOUT_SLOT_SHOWN = "Checkout.Slot Shown";
    String CHECKOUT_DEFAULT_SLOT_SELECTED = "Checkout.Default.Slot Selected";
    String CHECKOUT_SLOT_SELECTED = "Checkout.Slot Selected";
    String CHECKOUT_PAYMENT_SHOWN = "Checkout.Payment Shown";
    String CHECKOUT_VOUCHER_APPLIED = "Checkout.Voucher Applied";
    String CHECKOUT_VOUCHER_FAILED = "Checkout.Voucher Failed";
    String CHECKOUT_PAYMENT_GATEWAY_SUCCESS = "Checkout.Payment Gateway Success";
    String CHECKOUT_PAYMENT_GATEWAY_FAILURE = "Checkout.Payment Gateway Failure";
    String CHECKOUT_PAYMENT_GATEWAY_ABORTED = "Checkout.Payment Gateway Aborted";
    String CHECKOUT_PLACE_ORDER_CLICKED = "Checkout.Place Order Clicked";
    String CHECKOUT_PLACE_ORDER_AMOUNT_MISMATCH = "Checkout.Place Order Amount Mismatch";
    String CHECKOUT_ORDER_COMPLETE = "Checkout.Order Complete";
    String PLACE_ORDER = "placeorder";

    //thank you
    String THANK_YOU_PAGE_SHOWN = "Checkout.Thank You Page Shown";
    String BASKET_ADD_PROMO_BUNDLE = "Basket.Add Promo Bundle";

    String NOTIFICATION_ERROR = "Notification Error";


    //member referral
    String MEMBER_REFERRAL_SHOWN = "Member.Referral Shown";
    String MEMBER_REFERRAL_FREE_SMS_SHOWN = "Member.Referral free SMS Shown";
    String MEMBER_REFERRAL_WHATS_APP_SHOWN = "Member.Referral WhatsApp Shown";
    String MEMBER_REFERRAL_FACEBOOK_SHOWN = "Member.Referral Facebook Shown";
    String MEMBER_REFERRAL_BB_MAIL_SHOWN = "Member.Referral Mail Shown";
    String MEMBER_REFERRAL_GOOGLE_PLUS_SHOWN = "Member.Referral Google Plus Shown";
    String MEMBER_REFERRAL_GOOGLE_APP_SHOWN = "Member.Referral Google App Shown";
    String MEMBER_REFERRAL_HIKE_SHOWN = "Member.Referral Hike Shown";
    String MEMBER_REFERRAL_OTHER_SHOWN = "Member.Referral Shear via other Shown";


    void trackEvent(String eventName, Map<String, String> eventAttribs,
                    String source, String sourceValue, boolean isCustomerValueIncrease);

    void trackEvent(String eventName, Map<String, String> eventAttribs);
}
