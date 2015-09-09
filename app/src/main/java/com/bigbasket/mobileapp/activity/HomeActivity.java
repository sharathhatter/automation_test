package com.bigbasket.mobileapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.account.AddressSummaryDropdownAdapter;
import com.bigbasket.mobileapp.managers.AddressManager;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class HomeActivity extends BBActivity {

    private Spinner mSpinnerArea;
    private ProgressBar mProgressBarArea;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinnerArea = (Spinner) findViewById(R.id.spinnerArea);
        mProgressBarArea = (ProgressBar) findViewById(R.id.progressBarArea);

        mSpinnerArea.setVisibility(View.GONE);
        mProgressBarArea.setVisibility(View.VISIBLE);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_home_layout;
    }

    private void setUpAddressSpinner() {
        ArrayList<AddressSummary> addressSummaries = AddressManager.getStoredAddresses(this);

        if (addressSummaries != null && addressSummaries.size() > 0) {
            mSpinnerArea.setVisibility(View.VISIBLE);
            mProgressBarArea.setVisibility(View.GONE);
            AddressSummaryDropdownAdapter adapter = new
                    AddressSummaryDropdownAdapter(addressSummaries,
                    getString(R.string.changeMyLocation), this);
            mSpinnerArea.setAdapter(adapter);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("");
            }
        } else {
            setUpEmptyAddress();
        }
    }

    private void setUpEmptyAddress() {
        mSpinnerArea.setVisibility(View.GONE);
        mProgressBarArea.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mTitle);
        }
    }

    @Override
    public void onAddressSynced() {
        super.onAddressSynced();
        setUpAddressSpinner();
    }

    @Override
    public void onAddressSyncFailure() {
        super.onAddressSyncFailure();
        setUpEmptyAddress();
    }

    public void setTitle(String title) {
        if (mSpinnerArea.getVisibility() != View.VISIBLE && mProgressBarArea.getVisibility() != View.VISIBLE) {
            super.setTitle(title);
        }
    }

    @Override
    public void startFragment() {
        int fragmentCode = getIntent().getIntExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        startFragment(fragmentCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNextScreenNavigationContext(TrackEventkeys.HOME);
    }
}
