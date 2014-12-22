package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

public class DataUtil {

    private static HashMap<String, String> addBasketKeyHash = new HashMap<String, String>() {{
//        put(Productdetail.class.getSimpleName(), "pd");
//        put(SearchFragment.class.getSimpleName(), "ps");
//        put(BrowseByOffersActivity.class.getSimpleName(), "of");
//        put(ShoppingList.class.getSimpleName(), "sl");
//        put(CategoryProductsActivity.class.getSimpleName(), "pc");
//        put(ShowCartActivity.class.getSimpleName(), "vb");
//        put(QuickShopActivity.class.getSimpleName(), "qs");
//        put(PromoSetProductsActivity.class.getSimpleName(), "pp");
        // TODO : Fix this functionality for add to basket
    }};

    public static boolean isInternetAvailable(Context context) {
        return getConnectionStatus(context) == NetworkStatusCodes.NET_CONNECTED;
    }

    public static int getConnectionStatus(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (networkInfo.isConnected()) {
                return NetworkStatusCodes.NET_CONNECTED;
            } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                return NetworkStatusCodes.NET_CONNECTING;
            }
        }
        return NetworkStatusCodes.NET_DISCONNECTED;
    }

    public static String getAddBasketNavigationActivity(String activityName) {
        return addBasketKeyHash.get(activityName);
    }
}