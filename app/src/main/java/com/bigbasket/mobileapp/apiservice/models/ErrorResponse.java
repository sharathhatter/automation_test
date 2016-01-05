package com.bigbasket.mobileapp.apiservice.models;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ErrorResponse {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({API_ERROR, HTTP_ERROR})
    public @interface Type {
    }

    public static final int API_ERROR = 1;
    public static final int HTTP_ERROR = 2;

    private int code;
    private String message;
    private Throwable throwable;
    private @Type int errorType;

    public ErrorResponse(int code, String message, @Type int errorType) {
        this.code = code;
        this.message = message;
        this.errorType = errorType;
    }

    public ErrorResponse(Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean isException() {
        return throwable != null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Type
    public int getErrorType() {
        return errorType;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
