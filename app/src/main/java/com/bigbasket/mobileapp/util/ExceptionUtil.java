package com.bigbasket.mobileapp.util;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ExceptionUtil {

    public static final int SOCKET_EXP = 60;
    public static final int MISSING_FIELD = 118;
    public static final int SERVER_ERROR = 121;
    public static final int MEMBER_ERROR = 122;
    public static final int TIMEOUT_EXP = 402;
    public static final int UNKNOWN_HOST_EXP = 409;
    public static final int JSON_EXP = 800;
    public static final int GENERAL_EXP = 900;
    public static final int INVALID_CHAR_EXP = 901;
    public static final int IO_EXCEPTION_EXP = 902;

    public static final String SOCKET_EXP_MSG = "Connection timeout";
    public static final String MISSING_FIELD_MSG = "118";
    public static final String SERVER_ERROR_MSG = "121";
    public static final String MEMBER_ERROR_MSG = "122";
    public static final String TIMEOUT_EXP_MSG = "Connection timeout";
    public static final String UNKNOWN_HOST_EXP_MSG = "Unknown host";
    public static final String JSON_EXP_MSG = "Missing Field";
    public static final String GENERAL_EXP_MSG = "Service temporary not available";
    public static final String INVALID_CHAR_EXP_MSG = "901";
    public static final String IO_EXCEPTION_EXP_MSG = "Unable to establish connection.";
    public static final int PROMO_CATEGORY_NOT_EXIST = 124;
    public static final int INVALID_INPUT = 119;
    public static final int INVALID_FIELD = 103;
    public static final int INVALID_PROMO = 127;
    public static final int PROMO_NOT_ACTIVE = 126;
    public static final int PROMO_NOT_EXIST = 123;
    public static final int PROMO_SET_NOT_EXIST = 125;
    public static final int CART_NOT_EXISTS_ERROR = 106;
    public static final int INTERNAL_SERVER_ERROR = 101;
    public static final int EMPTY_ADDRESS = 130;


    public static String getExceptionMessage(Exception exception) {
        return getExceptionMessage(getExceptionNumber(exception));
    }


    private static String getExceptionMessage(int exceptionNumber) {
        switch (exceptionNumber) {
            case UNKNOWN_HOST_EXP:
                return UNKNOWN_HOST_EXP_MSG;

            case TIMEOUT_EXP:
                return TIMEOUT_EXP_MSG;

            case SOCKET_EXP:
                return SOCKET_EXP_MSG;

            case IO_EXCEPTION_EXP:
                return IO_EXCEPTION_EXP_MSG;

            case JSON_EXP:
                return JSON_EXP_MSG;

            default:
                return GENERAL_EXP_MSG;

        }
    }

    private static int getExceptionNumber(Exception exception) {

        if (exception instanceof UnknownHostException) {
            return UNKNOWN_HOST_EXP;
        } else if (exception instanceof ConnectTimeoutException) {
            return TIMEOUT_EXP;
        } else if (exception instanceof SocketTimeoutException) {
            return SOCKET_EXP;
        } else if (exception instanceof IOException) {
            return IO_EXCEPTION_EXP;
        } else if (exception instanceof JSONException) {
            return JSON_EXP;
        }

        return GENERAL_EXP;
    }
}
