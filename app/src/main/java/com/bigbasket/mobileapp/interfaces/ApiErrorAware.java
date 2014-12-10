package com.bigbasket.mobileapp.interfaces;

public interface ApiErrorAware {

    public void showApiErrorDialog(String message);

    public void showApiErrorDialog(String message, boolean finish);

    public void showApiErrorDialog(String message, String sourceName, Object valuePassed);

    public void showApiErrorDialog(String message, int resultCode);

}
