package com.bigbasket.mobileapp.interfaces;


import android.app.ProgressDialog;
import android.support.annotation.Nullable;

public interface ProgressIndicationAware {
    void showProgressDialog(String msg);

    void showProgressDialog(String msg, boolean cancelable);

    void showProgressDialog(String msg, boolean cancelable, boolean isDeterminate);

    @Nullable
    ProgressDialog getProgressDialog();

    void hideProgressDialog();

    void showProgressView();

    void hideProgressView();
}
