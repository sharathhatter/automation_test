package com.bigbasket.mobileapp.util;

public final class MobileApiUrl {

    public static final String DOMAIN = "http://dev1.bigbasket.com/";
    //public static final String DOMAIN = "http://test2.bigbasket.com/";
    public static final String API_VERSION = "v2.0.0";

    private static final String BASE_API_URL = DOMAIN + "mapi/";

    public static String getBaseAPIUrl() {
        return BASE_API_URL + API_VERSION + "/";
    }
}