package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAddressSummaryResponse;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChooseLocationActivity extends BackButtonActivity {

    private GoogleApiClient mGoogleApiClient;
    @Nullable
    private LatLng mSelectedLatLng;
    private AddressSummary mChosenAddressSummary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseYourLocation));

        buildGoogleApiClient();
        showProgressDialog(getString(R.string.readingYourCurrentLocation));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_choose_location;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public String getScreenTag() {
        return ChooseLocationActivity.class.getName();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            mSelectedLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            updateLocation(true);
        } else {
            hideProgressDialog();
            onLocationReadFailure();
        }
    }

    private void updateLocation(final boolean display) {
        if (mSelectedLatLng == null) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        if (!display) {
            showProgressDialog(getString(R.string.please_wait));
        }
        bigBasketApiService.setCurrentAddress(null, String.valueOf(mSelectedLatLng.latitude),
                String.valueOf(mSelectedLatLng.longitude), new Callback<ApiResponse<GetAddressSummaryResponse>>() {
                    @Override
                    public void success(ApiResponse<GetAddressSummaryResponse> getAddressSummaryApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getAddressSummaryApiResponse.status) {
                            case 0:
                                mChosenAddressSummary = getAddressSummaryApiResponse.apiResponseContent.addressSummaries.get(0);
                                if (display) {
                                    showSelectedLocation();
                                } else {
                                    onLocationChanged();
                                }
                                break;
                            default:
                                handler.sendEmptyMessage(getAddressSummaryApiResponse.status,
                                        getAddressSummaryApiResponse.message);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private void showSelectedLocation() {
        if (mChosenAddressSummary == null) return;
        TextView txtDetectedArea = (TextView) findViewById(R.id.txtDetectedArea);
        txtDetectedArea.setTypeface(faceRobotoMedium);
        txtDetectedArea.setText(mChosenAddressSummary.toString());
    }

    public void onLocationButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.btnToCurrentLocation:
                onLocationChanged();
                break;
            case R.id.btnChooseLocation:
                Intent intent = new Intent(this, PlacePickerApiActivity.class);
                startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
                break;
        }
    }

    private void onLocationChanged() {
        if (mChosenAddressSummary == null) return;
        City newCity = new City(mChosenAddressSummary.getCityName(),
                mChosenAddressSummary.getCityId());
        changeCity(newCity);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        hideProgressDialog();
    }

    private void onLocationReadFailure() {
        showToast(getString(R.string.unableToReadLocation));
        mSelectedLatLng = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED && data != null
                && data.getParcelableExtra(Constants.LAT) != null) {
            mSelectedLatLng = data.getParcelableExtra(Constants.LAT);
            updateLocation(false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
