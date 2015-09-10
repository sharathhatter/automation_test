package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ChooseLocationActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.account.AddressSummaryDropdownAdapter;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.managers.AddressManager;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class HomeActivity extends BBActivity implements OnAddressChangeListener {

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
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_home_layout;
    }

    private void setUpAddressSpinner() {
        final ArrayList<AddressSummary> addressSummaries = AddressManager.getStoredAddresses(this);

        if (addressSummaries != null && addressSummaries.size() > 0) {
            mSpinnerArea.setVisibility(View.VISIBLE);
            mProgressBarArea.setVisibility(View.GONE);
            final AddressSummaryDropdownAdapter adapter = new
                    AddressSummaryDropdownAdapter(addressSummaries,
                    getString(R.string.changeMyLocation), this);
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

    private void changeAddressRequested() {
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            Intent intent = new Intent(this, ChooseLocationActivity.class);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else {
            Intent intent = new Intent(this, BackButtonActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
            intent.putExtra(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.ADDRESS_SELECT);
            startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
        }
    }

    private void setCurrentDeliveryAddress(AddressSummary addressSummary) {
        String addressId, lat, lng;
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            addressId = null;
            lat = String.valueOf(addressSummary.getLatitude());
            lng = String.valueOf(addressSummary.getLongitude());
        } else {
            addressId = addressSummary.getId();
            lat = lng = null;
        }
        new ChangeAddressTask<>(this, addressId, lat, lng).startTask();
    }

    private void setCurrentDeliveryAddress(String addressId) {
        new ChangeAddressTask<>(this, addressId, null, null).startTask();
    }

    @Override
    public void onAddressChanged(ArrayList<AddressSummary> addressSummaries) {
        if (addressSummaries != null && addressSummaries.size() > 0) {
            City newCity = new City(addressSummaries.get(0).getCityName(),
                    addressSummaries.get(0).getCityId());
            changeCity(newCity);
        } else {
            showToast(getString(R.string.unknownError));
        }
    }

    @Override
    public void onAddressNotSupported(String msg) {
        showAlertDialog(msg);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED && data != null) {
            String addressId = data.getStringExtra(Constants.ADDRESS_ID);
            if (!TextUtils.isEmpty(addressId)) {
                setCurrentDeliveryAddress(addressId);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
