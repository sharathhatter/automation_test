package com.bigbasket.mobileapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.SearchActivity;
import com.bigbasket.mobileapp.adapter.account.AddressSummaryDropdownAdapter;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends SearchActivity {

    private Spinner mSpinnerArea;
    private ProgressBar mProgressBarArea;
    private int mCurrentSpinnerIdx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinnerArea = (Spinner) findViewById(R.id.spinnerArea);
        mProgressBarArea = (ProgressBar) findViewById(R.id.progressBarArea);

        mSpinnerArea.setVisibility(View.GONE);
        mProgressBarArea.setVisibility(View.VISIBLE);
        mCurrentSpinnerIdx = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(Constants.APP_LAUNCH, false)) {
            HashMap<String, Object> appFlyerHashMap = new HashMap<>();
            appFlyerHashMap.put(Constants.CITY, preferences.getString(Constants.CITY, ""));
            trackEventAppsFlyer(TrackingAware.APP_OPEN, appFlyerHashMap);
            preferences.edit().putBoolean(Constants.APP_LAUNCH, false).apply();
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_home_layout;
    }

    private void setUpAddressSpinner() {
        mCurrentSpinnerIdx = 0;
        final ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(this).getAddressSummaries();

        boolean isGuest = AuthParameters.getInstance(this).isAuthTokenEmpty();
        if (addressSummaries != null && addressSummaries.size() > 0) {
            mSpinnerArea.setVisibility(View.VISIBLE);
            mProgressBarArea.setVisibility(View.GONE);
            final AddressSummaryDropdownAdapter adapter = new
                    AddressSummaryDropdownAdapter<>(this, R.layout.uiv3_change_address_spinner_row,
                    addressSummaries,
                    isGuest ? getString(R.string.changeMyLocation) : getString(R.string.changeMyAddress));
            mSpinnerArea.setAdapter(adapter);
            mSpinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == Spinner.INVALID_POSITION || position == mCurrentSpinnerIdx)
                        return;
                    if (adapter.getSpinnerViewType(position) == AddressSummaryDropdownAdapter.VIEW_TYPE_ADDRESS) {
                        mCurrentSpinnerIdx = position;
                        setCurrentDeliveryAddress(addressSummaries.get(position));
                    } else {
                        changeAddressRequested();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
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
    public void onNoBasketUpdate() {
        super.onNoBasketUpdate();
        if (mSpinnerArea != null && mSpinnerArea.getAdapter() != null
                && mSpinnerArea.getAdapter().getCount() > 0) {
            mCurrentSpinnerIdx = 0;
            mSpinnerArea.setSelection(0);
        }
    }

    private void setCurrentDeliveryAddress(AddressSummary addressSummary) {
        String addressId, lat, lng;
        boolean isTransient;
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            addressId = null;
            lat = String.valueOf(addressSummary.getLatitude());
            lng = String.valueOf(addressSummary.getLongitude());
            isTransient = false;
        } else {
            addressId = addressSummary.getId();
            lat = lng = null;
            isTransient = true;
        }
        new ChangeAddressTask<>(this, addressId, lat, lng, null, isTransient).startTask();
    }

    @Override
    public void onDataSynced() {
        super.onDataSynced();
        setUpAddressSpinner();
    }

    @Override
    public void onDataSyncFailure() {
        super.onDataSyncFailure();
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
        setCurrentScreenName(TrackEventkeys.HOME);
        // Check if spinner's index is set to "CHANGE MY LOCATION"
        // In case it is True, then reset it to 0, and ensure that spinner's on item-selection
        // even is NOT triggered
        if (mSpinnerArea != null && mSpinnerArea.getSelectedItemPosition() > 0
                && mSpinnerArea.getAdapter() instanceof AddressSummaryDropdownAdapter) {
            AddressSummaryDropdownAdapter adapter = (AddressSummaryDropdownAdapter) mSpinnerArea.getAdapter();
            if (adapter.getSpinnerViewType(mSpinnerArea.getSelectedItemPosition())
                    == AddressSummaryDropdownAdapter.VIEW_TYPE_CHANGE) {
                mCurrentSpinnerIdx = 0;
                mSpinnerArea.setSelection(0);
            }
        }
    }

    @Override
    protected String getCategoryId() {
        return getString(R.string.home);
    }
}
