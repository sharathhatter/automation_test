package com.bigbasket.mobileapp.util;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by muniraju on 11/03/16.
 */
public class SignInUtils {
    // An integer error argument for play services error dialog
    public static final String DIALOG_ERROR = "dialog_error";
    public static final String PLUS_BASE_TAG = "PlusBaseActivity";

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SIGN_IN, SIGN_OUT, REVOKE})
    public @interface ConnectionMode {
    }

    public static final int NONE = 0;
    public static final int SIGN_IN = 1;
    public static final int REVOKE = 2;
    public static final int SIGN_OUT = 3;


    /* Keys for persisting instance variables in savedInstanceState */
    public static final String KEY_IS_RESOLVING = "is_resolving";
    public static final String KEY_CONNECTION_MODE = "plus_base_connection_mode";
    public static final String KEY_SHOW_INITIAL_INFO = "key_initial_permission_info";
}
