package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
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
    private AddressSummary mChosenAddressSummary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseYourLocation));

        renderLocation();
    }

    private void renderLocation() {
        if (!DataUtil.isLocationServiceEnabled(this)) {
            showAlertDialog(getString(R.string.enableLocationHeading),
                    getString(R.string.enableLocation),
                    DialogButton.YES, DialogButton.CANCEL, Constants.LOCATION, null,
                    getString(R.string.enable));
        } else {
            showProgressDialog(getString(R.string.readingYourCurrentLocation));
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                updateLastKnownLocation();
            } else {
                buildGoogleApiClient();
            }
        }
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            renderLocation();
        }
        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_choose_location;
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
        updateLastKnownLocation();
    }

    private void updateLastKnownLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            updateLocation(true, latLng);
        } else {
            hideProgressDialog();
            onLocationReadFailure();
        }
    }

    private void updateLocation(final boolean isSomeoneAlreadyShowingProgressBar, LatLng latLng) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        if (!isSomeoneAlreadyShowingProgressBar) {
            showProgressDialog(getString(R.string.please_wait));
        }
        bigBasketApiService.setCurrentAddress(null, String.valueOf(latLng.latitude),
                String.valueOf(latLng.longitude), new Callback<ApiResponse<GetAddressSummaryResponse>>() {
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
                                if (isSomeoneAlreadyShowingProgressBar) {
                                    showSelectedLocation(null);
                                } else {
                                    onLocationChanged();
                                }
                                break;
                            case ApiErrorCodes.ADDRESS_NOT_SERVED:
                                showSelectedLocation(getAddressSummaryApiResponse.message);
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

    private void showSelectedLocation(@Nullable String errMsg) {
        TextView lblCurrentLocation = (TextView) findViewById(R.id.lblCurrentLocation);
        TextView txtDetectedArea = (TextView) findViewById(R.id.txtDetectedArea);
        TextView txtNotServedMsg = (TextView) findViewById(R.id.txtNotServedMsg);

        lblCurrentLocation.setTypeface(faceRobotoRegular);
        txtDetectedArea.setTypeface(faceRobotoMedium);

        if (!TextUtils.isEmpty(errMsg)) {
            lblCurrentLocation.setVisibility(View.GONE);
            txtDetectedArea.setVisibility(View.GONE);
            txtNotServedMsg.setVisibility(View.VISIBLE);
            txtNotServedMsg.setText(errMsg);
        } else if (mChosenAddressSummary != null) {
            lblCurrentLocation.setVisibility(View.VISIBLE);
            txtDetectedArea.setVisibility(View.VISIBLE);
            txtNotServedMsg.setVisibility(View.GONE);
            String area = !TextUtils.isEmpty(mChosenAddressSummary.getArea()) ?
                    mChosenAddressSummary.getArea() + ", " : "";
            txtDetectedArea.setText(area + mChosenAddressSummary.getCityName());
        }
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
        if (mChosenAddressSummary == null) {
            Snackbar.make(findViewById(R.id.layoutChooseLocation),
                    R.string.pleaseChooseLocationToProceed, Snackbar.LENGTH_SHORT).show();
            return;
        }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED && data != null
                && data.getParcelableExtra(Constants.LAT) != null) {
            LatLng latLng = data.getParcelableExtra(Constants.LAT);
            updateLocation(false, latLng);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null && sourceName.equals(Constants.LOCATION)) {
            try {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                showToast(getString(R.string.locationSettingError));
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }
}
