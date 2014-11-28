package com.bigbasket.mobileapp.interfaces;

import java.util.Map;

public interface TrackingAware {

    public static final String BASKET_INCREMENT = "Basket.Increment";
    public static final String BASKET_DECREMENT = "Basket.Decrement";
    public static final String BASKET_REMOVE = "Basket.Remove";
    public static final String BASKET_ADD_PROMO_BUNDLE = "Basket.Add Promo Bundle";
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
    public static final String CHECKOUT_ORDER_COMPLETE = "Checkout.Order Complete";
    public static final String ORDER_ACTIVE_ORDERS_SHOWN = "Order.Active Orders Shown";
    public static final String ORDER_PAST_ORDERS = "Order.Past Orders";
    public static final String ORDER_VIEW_INVOICE = "Order.View Invoice";
    public static final String MY_ACCOUNT_CHANGE_PASSWD_SELECTED = "MyAccount.Change Password Selected";
    public static final String MY_ACCOUNT_CHANGE_PASSWD_SUCCESS = "MyAccount.Change Password Success";
    public static final String MY_ACCOUNT_CHANGE_PASSWD_FAILED = "MyAccount.Change Password Failed";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_SELECTED = "MyAccount.Update Profile Selected";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_SUCCESS = "MyAccount.Update Profile Success";
    public static final String MY_ACCOUNT_UPDATE_PROFILE_FAILED = "MyAccount.Update Profile Failed";
    public static final String MY_ACCOUNT_CHANGE_PIN_SELECTED = "MyAccount.Change Pin Selected";
    public static final String MY_ACCOUNT_CHANGE_PIN_SUCCESS = "MyAccount.Change Pin Success";
    public static final String MY_ACCOUNT_CHANGE_PIN_FAILED = "MyAccount.Change Pin Failed";
    public static final String MY_ACCOUNT_WALLET_ACTIVITY_SHOWN = "MyAccount.Wallet Activity Shown";
    public static final String MY_ACCOUNT_DELIVERY_ADDRESS_SHOWN = "MyAccount.Delivery Addresses Shown";
    public static final String MY_ACCOUNT_ADDRESS_EDITED = "MyAccount.Address Edited";
    public static final String MY_ACCOUNT_ADDRESS_CREATED = "MyAccount.Address Created";
    public static final String MY_ACCOUNT_LOGIN_SHOWN = "MyAccount.Login Shown";
    public static final String MY_ACCOUNT_LOGIN_SUCCESS = "MyAccount.Login Success";
    public static final String MY_ACCOUNT_LOGIN_FAILED = "MyAccount.Login Failed";
    public static final String MY_ACCOUNT_REGISTRATION_SHOWN = "MyAccount.Registration Shown";
    public static final String MY_ACCOUNT_REGISTRATION_SUCCESS = "MyAccount.Registration Success";
    public static final String MY_ACCOUNT_REGISTRATION_FAILED = "MyAccount.Registration Failed";
    public static final String MY_ACCOUNT_FACEBOOK_LOGIN = "MyAccount.Facebook Login";
    public static final String MY_ACCOUNT_GOOGLE_LOGIN = "MyAccount.Google Login";
    public static final String MY_ACCOUNT_FACEBOOK_LOGIN_SUCCESS = "MyAccount.Facebook Login Success";
    public static final String MY_ACCOUNT_GOOGLE_LOGIN_SUCCESS = "MyAccount.Google Login Success";
    public static final String MY_ACCOUNT_FACEBOOK_LOGIN_FAILED = "MyAccount.Facebook Login Failed";
    public static final String MY_ACCOUNT_GOOGLE_LOGIN_FAILED = "MyAccount.Google Login Failed";
    public static final String MY_ACCOUNT_LOGOUT = "MyAccount.Logout";
    public static final String MY_ACCOUNT_FACEBOOK_LOGOUT = "MyAccount.Facebook Account Logout";
    public static final String MY_ACCOUNT_GOOGLE_LOGOUT = "MyAccount.Google Account Logout";
    public static final String PROMO_CATEGORY_LIST = "Promo.Category List";
    public static final String PROMO_DETAIL = "Promo.Promo Detail";
    public static final String PROMO_SET_PRODUCTS_SHOWN = "Promo.Promo Set Products Shown";
    public static final String SHOP_LST_SHOWN = "ShoppingList.All Lists Shown";
    public static final String SHOP_LST_SUMMARY_SHOWN = "ShoppingList.Summary Shown";
    public static final String SHOP_LST_SYSTEM_LST_SHOWN = "ShoppingList.System ShoppingList.Summary Shown";
    public static final String SHOP_LST_CATEGORY_DETAIL = "ShoppingList.Category Details";
    public static final String SHOP_LST_SYSTEM_LIST_CATEGORY_DETAIL = "ShoppingList.System ShoppingList.Category Details";
    public static final String SHOP_LST_NAME_CHANGED = "ShoppingList.Name Changed";
    public static final String SHOP_LST_DELETED = "ShoppingList.List Deleted";
    public static final String SHOP_LST_CREATED = "ShoppingList.List Created";
    public static final String SHOP_LIST_PRODUCT_ADDED = "ShoppingList.Product Added";

    public void trackEvent(String eventName, Map<String, String> eventAttribs,
                           String source, String sourceValue);

    public void trackEvent(String eventName, Map<String, String> eventAttribs);
}
