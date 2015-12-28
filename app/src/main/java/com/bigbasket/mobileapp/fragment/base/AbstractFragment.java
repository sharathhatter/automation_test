package com.bigbasket.mobileapp.fragment.base;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;


public abstract class AbstractFragment extends Fragment implements AppOperationAware {

    protected static Typeface faceRupee;
    protected static Typeface faceRobotoRegular;
    protected static Typeface faceRobotoMedium;

    private boolean mAlreadyLoaded = false;
    private boolean isFragmentSuspended;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAlreadyLoaded = savedInstanceState.getBoolean(Constants.FRAGMENT_STATE);
        }
        isFragmentSuspended = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentSuspended = false;
    }

    public void onBackStateChanged() {
        isFragmentSuspended = false;
        if (mAlreadyLoaded) {
            onBackResume();
        } else {
            mAlreadyLoaded = true;
        }
    }

    protected void onBackResume() {
        isFragmentSuspended = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentSuspended = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isFragmentSuspended = false;
        faceRupee = FontHolder.getInstance(getActivity()).getFaceRupee();
        faceRobotoRegular = FontHolder.getInstance(getActivity()).getFaceRobotoRegular();
        faceRobotoMedium = FontHolder.getInstance(getActivity()).getFaceRobotoMedium();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        isFragmentSuspended = false;
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isSuspended() {
        return getActivity() != null && ((BaseActivity) getActivity()).isSuspended() || isFragmentSuspended;
    }

    public void setSuspended(boolean isFragmentSuspended) {
        this.isFragmentSuspended = isFragmentSuspended;
    }

    public BaseActivity getCurrentActivity() {
        return (BaseActivity) getActivity();
    }

    protected void finish() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.FRAGMENT_STATE, mAlreadyLoaded);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    public abstract String getFragmentTxnTag();
}