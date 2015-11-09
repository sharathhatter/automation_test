package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;

public interface AppOperationAware {
    BaseActivity getCurrentActivity();

    boolean isSuspended();

    void setSuspended(boolean state);

    boolean checkInternetConnection();

    BigBasketMessageHandler getHandler();

    void showProgressDialog(String msg);

    void showProgressDialog(String msg, boolean cancelable);

    void showProgressDialog(String msg, boolean cancelable, boolean isDeterminate);

    void hideProgressDialog();

    void showProgressView();

    void hideProgressView();
}
