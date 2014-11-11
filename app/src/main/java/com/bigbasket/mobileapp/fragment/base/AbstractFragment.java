package com.bigbasket.mobileapp.fragment.base;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.util.Constants;


public abstract class AbstractFragment extends Fragment {

    public static Typeface faceRupee;
    public static Typeface faceRobotoRegular;
    public static Typeface faceRobotoThin;

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

    public void onBackResume() {
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

        AssetManager assetManager = getActivity().getAssets();
        faceRupee = Typeface.createFromAsset(assetManager, "Rupee.ttf");
        faceRobotoRegular = Typeface.createFromAsset(assetManager, "Roboto-Regular.ttf");
        faceRobotoThin = Typeface.createFromAsset(assetManager, "Roboto-Thin.ttf");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        isFragmentSuspended = true;
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setFragmentSuspended(boolean isFragmentSuspended) {
        this.isFragmentSuspended = isFragmentSuspended;
        if (getActivity() == null || getBaseActivity() == null) return;
        getBaseActivity().setActivitySuspended(isFragmentSuspended);
    }

    public boolean isSuspended() {
        return getActivity() != null && ((BaseActivity) getActivity()).isActivitySuspended() || isFragmentSuspended;
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void finish() {
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