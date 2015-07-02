package com.bigbasket.mobileapp.interfaces;


public interface ProgressIndicationAware {
    void showProgressDialog(String msg);

    void showProgressDialog(String msg, boolean cancelable);

    void hideProgressDialog();

    void showProgressView();

    void hideProgressView();
}
