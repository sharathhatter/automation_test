package com.bigbasket.mobileapp.interfaces;


public interface ProgressIndicationAware {
    public void showProgressDialog(String msg);

    public void hideProgressDialog();

    public void showProgressView();

    public void hideProgressView();
}
