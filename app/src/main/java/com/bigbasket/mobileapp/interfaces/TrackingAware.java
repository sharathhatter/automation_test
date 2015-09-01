package com.bigbasket.mobileapp.interfaces;

import java.util.Map;

public interface TrackingAware {


    //Start Activity
    String APP_OPEN = "App.Open";
    String ENTRY_PAGE_SHOWN = "EntryPage.Shown";
    String ENTRY_PAGE_SKIP_BUTTON_CLICKED = "EntryPage.Skip Clicked";
    String ENTRY_PAGE_LOGIN_CLICKED = "EntryPage.Login Clicked";
    String ENTRY_PAGE_SIGNUP_CLICKED = "EntryPage.Signup Clicked";

    //Home Screen topnav
    String COMMUNICATION_HUB_SHOWN = "CommunicationHub.Shown";
    String LOGIN_CLICKED = "Login.Clicked";
    String HOME_CHANGE_CITY = "Change City.Clicked";
    String RATE_APP_CLICKED = "Rate App.Clicked";

    String DISCOUNT_SHOWN = "Discounts Page.Shown";
    String SEARCH_SHOWN = "Search Page.Shown";

    //change city dialog
    String CHANGE_CITY_SHOWN = "Select City.Shown";
    String CHANGE_CITY_CLICKED = "Select City.Continue";

    //login and register
    String LOGIN_SHOWN = "Login Page.Shown";
    String LOGIN_BTN_CLICKED = "Login Button.Clicked";
    String LOGIN_FAILED = "Login Failed";
    String SHOW_PASSWORD_ENABLED = "Show Password.Clicked";
    String LOGIN_REMEMBER_ME_ENABLED = "Remember Me.Enabled";
    String FORGOT_PASSWORD_CLICKED = "Forgot PassWord.Clicked";
    //String NEW_USER_REGISTER_CLICKED = "Login.New User Register Clicked";
    String FORGOT_PASSWORD_DIALOG_SHOWN = "Forgot Password.Dialog Shown";
    String FORGOT_PASSWORD_EMAIL_CLICKED = "Forgot Password.Email New Password Clicked";
    String REGISTRATION_PAGE_SHOWN = "Registration Page.Shown";
    String PROMO_MAILER_ENABLED = "Promotional Mailer Enabled";
    String REGISTER_BTN_CLICK = "SignUp Page.SignUp Clicked";
    String REGISTRATION_FAILED = "SignUp Page.Failed";


    //Account
    String MY_ACCOUNT_CLICKED = "My Account.Clicked";
    String MY_ACCOUNT_SHOWN = "My Account.Shown";
    String MY_ORDER_CLICKED = "My Orders.Clicked";
    String MY_ORDER_SHOWN = "My Orders.Shown";
    String MY_ORDER_ITEM_CLICKED = "My Orders Item.Clicked";
    String MY_ACCOUNT_UPDATE_PROFILE_CLICKED = "Edit Profile.Clicked";


    //update_profile
    String UPDATE_PROFILE_SHOWN = "Update Profile.Shown";
    String UPDATE_PROFILE_GET_FAILED = "Update Profile.Failed";
    String UPDATE_PROFILE_SUBMIT_BTN_CLICKED = "Update Profile.Clicked";
    //otp dialog
    String OTP_DIALOG_SHOWN = "OTPDialog.Shown";
    String OTP_SUBMIT_BTN_CLICKED = "OTP Submit.Clicked";

    //change password
    String CHANGE_PASSWORD_CLICKED = "Change Password.Clicked";
    String CHANGE_PASSWORD_SHOWN = "Change Password.Shown";
    String CHANGE_PASSWORD_FAILED = "Change Password.Update";

    //wallet
    String MY_ACCOUNT_WALLET_CLICKED = "My Wallet.Clicked";
    String WALLET_SUMMARY_SHOWN = "My Wallet Summary.Shown";

    //Address
    String DELIVERY_ADDRESS_CLICKED = "Delivery Addresses.Clicked";
    String DELIVERY_ADDRESS_SHOWN = "Delivery Addresses.Shown";
    String NEW_ADDRESS_CLICKED = "Address New.Clicked";
    String ENABLE_DEFAULT_ADDRESS = "Make This Default Address.Clicked";
    String NEW_ADDRESS_FAILED = "New Address Add.Failed";
    String UPDATE_ADDRESS_FAILED = "Update Address.Failed";

    //topnav and home page
    String SHOPPING_LIST_ICON_CLICKED = "Shopping Lists.Clicked";
    String SMART_BASKET_ICON_CLICKED = "Smart Basket.Clicked";

    //logout
    String LOG_OUT_ICON_CLICKED = "Logout.Clicked";

    //active and past order
    String ORDER_SUMMARY_SHOWN = "Order.Order Details Shown";
    String ORDER_ITEMS_TAB_CHANGED = "Order Items.Tab Changed";
    String SHOP_FROM_PAST_ORDER = "Shop from This Order Shown";

    //shopping list
    String SHOP_LST_SHOWN = "ShoppingList.All Shown";
    String SHOP_LST_CREATED = "ShoppingList.Created";
    String SHOP_LST_NAME_CHANGED = "ShoppingList.Name Changed";
    String SHOP_LST_DELETED = "ShoppingList.Deleted";
    String SHOP_LST_SUMMARY_SHOWN = "ShoppingList.Summary Shown";


    //main menu
    String MENU_SHOWN = "Menu.Shown";
    String MENU_ITEM_CLICKED = "Menu Item.Clicked";

    // Dynamic Page
    String ITEM_CLICKED = "Item Clicked";

    //Home Page
    String HOME_PAGE_SHOWN = "HomePage.Shown";
    String DYNAMIC_SCREEN_SHOWN = "Dynamic Screen.Shown";
    String HOME_PAGE_ITEM_CLICKED = "HomePage.Item Clicked";
    String SEARCH = "Search";

    String DISCOUNT_TAB_CHANGED = "Discounts Tab.Changed";
    //product listing page
    String PRODUCT_LIST_SHOWN = "Product List.Shown";
    String PRODUCT_LIST_TAB_CHANGED = "Product List Tab.Changed";
    String PRODUCT_LIST_HEADER = "Spinner";
    String PRODUCT_DETAIL_SHOWN = "PD.Shown";
    String PRODUCT_LIST_HEADER_CLICKED = "Spinner.Clicked";
    String PRODUCT_LIST_FILTER_CLICKED = "Filter.Clicked";
    String TAB_CHANGED = "Tab Changed";
    //promo
    String PROMO_DETAIL_SHOWN = "Promo Detail.Shown";
    String PROMO_SET_PRODUCTS_SHOWN = "Promo Set.Shown";
    String PROMO_CATEGORY_LIST = "Promo Category.Shown";
    String PROMO_REDEEMED = "Promo.Redeemed";


    //basket
    String BASKET_INCREMENT = "Basket.Increment";
    String BASKET_ADD = "Basket.add";
    String BASKET_DECREMENT = "Basket.Decrement";
    String BASKET_REMOVE = "Basket.Remove Item";
    String BASKET_EMPTY_CLICKED = "Basket.Empty";
    String BASKET_VIEW_CLICKED = "Basket.Clicked";
    String BASKET_VIEW_SHOWN = "Basket.Shown";
    String BASKET_CHECKOUT_CLICKED = "Basket.Checkout Clicked";

    //subCat
    String CATEGORY_LANDING_SHOWN = "Category.Landing Shown";

    //smart and shopping list Basket
    String SMART_BASKET = "SmartBasket";
    String SHOPPING_LIST = "ShoppingList";
    String SMART_BASKET_SUMMARY_SHOWN = "SmartBasket.Summary Shown";
    String ADD_TO_SHOPPING_LIST = "Add To ShoppingList.Clicked";

    //product page dialog
    String FILTER_APPLIED = "Filter.Applied";
    String FILTER_CLEARED = "Filter.Cleared";
    String SORT_BY = "SortBy.Selected";

    //check out
    String CHECKOUT_QC_SHOWN = "Checkout.QC Shown";
    String CHECKOUT_CREATE_ADDRESS_SHOWN = "Checkout.Address Shown";
    String CHECKOUT_ADDRESS_CREATED = "Checkout.Address Created";
    String CHECKOUT_SLOT_SHOWN = "Checkout.Slot Shown";
    String CHECKOUT_SLOT_SELECTED = "Checkout.Slot Selected";
    String CHECKOUT_PAYMENT_SHOWN = "Checkout.Payment Shown";
    String CHECKOUT_VOUCHER_FAILED = "Checkout.Voucher Failed";
    String CHECKOUT_PLACE_ORDER_CLICKED = "Checkout.Place Order Clicked";
    String CHECKOUT_ORDER_COMPLETE = "Checkout.Order Completed";
    String PLACE_ORDER = "placeorder";

    String CHECKOUT_ADDRESS_CLICKED_CONTI = "Checkout.Address Continue Clicked ";
    String CHECKOUT_ADDRESS_SELECTED = "Checkout.Address Selected";
    String CHECKOUT_SLOT_SELECTED_CLICKED = "Checkout.Delivery Options Continue Clicked";
    String CHECKOUT_DELIVERY_OPTION_SHOWN = "Checkout.Delivery Options Shown";

    //thank you
    String THANK_YOU_PAGE_SHOWN = "Checkout.Thank You Page Shown";
    String BASKET_ADD_PROMO_BUNDLE = "Promo.Add Bundle";
    String CHECKOUT_KNOW_MORE_LINK_CLICKED = "Checkout.Know More Link Clicked";

    //eVoucher
    String EVOUCHER_SHOWN = "EVocuher.Shown";

    String NOTIFICATION_ERROR = "Notification Error";

    String FLAT_PAGE_SHOWN = "Flat Page Shown";
    String PAY_NOW_SHOWN = "PayNow.Shown";
    String PAY_NOW_DONE = "PayNow.Done";

    String FUND_WALLET_SHOWN = "Fund Wallet.Shown";
    String FUND_WALLET_DONE = "Fund Wallet.Done";

    void trackEvent(String eventName, Map<String, String> eventAttribs,
                    String source, String sourceValue, boolean isCustomerValueIncrease,
                    boolean sendToFacebook);

    void trackEvent(String eventName, Map<String, String> eventAttribs,
                    String source, String sourceValue, boolean isCustomerValueIncrease);

    void trackEvent(String eventName, Map<String, String> eventAttribs,
                    String source, String sourceValue, String nc, boolean isCustomerValueIncrease,
                    boolean sendToFacebook);

    void trackEvent(String eventName, Map<String, String> eventAttribs,
                    String source, String sourceValue);

    void trackEvent(String eventName, Map<String, String> eventAttribs);

    void trackEventAppsFlyer(String eventName);
}