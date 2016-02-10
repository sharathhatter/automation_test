package com.bigbasket.mobileapp.activity.account.uiv3;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import retrofit2.Call;

public class ChooseLocationActivity extends BackButtonActivity implements OnAddressChangeListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private AddressSummary mChosenAddressSummary;
    private boolean mIsFirstTime;
    private boolean mIsViaOnActivityResult;
    private boolean setAsCurrentAddressBoolean;
    private boolean autoModeBoolean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseDeliveryLocation));
        mIsFirstTime = getIntent().getBooleanExtra(Constants.IS_FIRST_TIME, false);
        if (savedInstanceState == null) {
            handlePermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    getString(R.string.location_permission_rationale),
                    Constants.PERMISSION_REQUEST_CODE_ACCESS_LOCATION);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (!mIsViaOnActivityResult
                && hasPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            triggerLocationFetching();
        }
    }

    private void triggerLocationFetching() {

        int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCurrentActivity());
        switch (playServicesAvailable) {
            case ConnectionResult.SUCCESS:
                renderLocation();
                break;
            default:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playServicesAvailable,
                        getCurrentActivity(), NavigationCodes.GO_TO_HOME);
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }


    private void renderLocation() {
        if (isSuspended()) return;
        if (!DataUtil.isLocationServiceEnabled(this)) {
            showAlertDialog(getString(R.string.enableLocationHeading),
                    getString(R.string.enableLocation),
                    DialogButton.YES, DialogButton.CANCEL, Constants.LOCATION_DIALOG_REQUEST, null,
                    getString(R.string.enable));
        } else {
            if (hasPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showProgressDialog(getString(R.string.readingYourCurrentLocation));
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    updateLastKnownLocation(false, false);
                } else {
                    buildGoogleApiClient();
                }
            } else {
                onLocationReadFailure();
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
            if(handlePermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    getString(R.string.location_permission_rationale),
                    Constants.PERMISSION_REQUEST_CODE_ACCESS_LOCATION)) {
                triggerLocationFetching();
            }
        } else if (item.getItemId() == android.R.id.home) {
            setResult(NavigationCodes.RESULT_CHANGE_CITY_CANCELLED);
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
    public void onBackPressed() {
        setResult(NavigationCodes.RESULT_CHANGE_CITY_CANCELLED);
        super.onBackPressed();
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
        updateLastKnownLocation(false, false);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void updateLastKnownLocation(boolean setAsCurrentAddress, boolean autoMode) {
        if (mGoogleApiClient == null) return;
        setAsCurrentAddressBoolean = setAsCurrentAddress;
        autoModeBoolean = autoMode;

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            if (setAsCurrentAddress) {
                updateLocation(latLng,
                        mChosenAddressSummary != null ? mChosenAddressSummary.getArea() : null);
            } else {
                getCurrentLocationDetail(latLng);
            }
        } else {
            if (autoMode) {
                triggerLocationFetching();
            } else {
                hideProgressDialog();
                onLocationReadFailure();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_ACCESS_LOCATION:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        updateLastKnownLocation(setAsCurrentAddressBoolean, autoModeBoolean);
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        hideProgressDialog();
                        onLocationReadFailure();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getCurrentLocationDetail(LatLng latLng) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Call<ApiResponse<AddressSummary>> call =
                bigBasketApiService.getLocationDetail(getPreviousScreenName(), String.valueOf(latLng.latitude),
                        String.valueOf(latLng.longitude));
        call.enqueue(new BBNetworkCallback<ApiResponse<AddressSummary>>(this) {
            @Override
            public void onSuccess(ApiResponse<AddressSummary> addressSummaryApiResponse) {
                switch (addressSummaryApiResponse.status) {
                    case 0:
                        mChosenAddressSummary = addressSummaryApiResponse.apiResponseContent;
                        showSelectedLocation(null);
                        break;
                    case ApiErrorCodes.ADDRESS_NOT_SERVED:
                        showSelectedLocation(addressSummaryApiResponse.message);
                        break;
                    default:
                        handler.sendEmptyMessage(addressSummaryApiResponse.status,
                                addressSummaryApiResponse.message);
                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    private void updateLocation(LatLng latLng, @Nullable String area) {
        boolean isTransientCall = !mIsFirstTime;
        new ChangeAddressTask<>(this,
                null, String.valueOf(latLng.latitude),
                String.valueOf(latLng.longitude), area, isTransientCall).startTask();
    }

    @Override
    public void onAddressChanged(ArrayList<AddressSummary> addressSummaries, String selectedAddressId) {
        if (addressSummaries != null && addressSummaries.size() > 0) {
            mChosenAddressSummary = addressSummaries.get(0);
            onLocationChanged();
        } else {
            showToast(getString(R.string.unknownError));
        }
    }

    @Override
    public void onAddressNotSupported(String msg) {
        showSelectedLocation(msg);
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
                if(handlePermission(Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.location_permission_rationale),
                        Constants.PERMISSION_REQUEST_CODE_ACCESS_LOCATION)) {
                    triggerLocationFetching();
                }
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
        requestCityChange(newCity);
    }

    public void requestCityChange(City newCity) {
        boolean reopenLandingPage = getIntent().getBooleanExtra(Constants.REOPEN_LANDING_PAGE, false);
        changeCity(newCity);
        if (reopenLandingPage) {
            setResult(NavigationCodes.LOCATION_CHOSEN);
            finish();
        } else {
            goToHome();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        hideProgressDialog();
        onLocationReadFailure();
    }

    private void onLocationReadFailure() {
        Snackbar.make(findViewById(R.id.layoutChooseLocation),
                R.string.unableToReadLocation, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onStateNotSaved();
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED) {
            mIsViaOnActivityResult = true;
            if (data != null && data.hasExtra(Constants.LAT)) {
                LatLng latLng = data.getParcelableExtra(Constants.LAT);
                updateLocation(latLng, data.getStringExtra(Constants.AREA));
            } else {
                buildGoogleApiClient();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        if (sourceName == Constants.LOCATION_DIALOG_REQUEST) {
            try {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(R.id.layoutChooseLocation),
                        R.string.locationSettingError, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onPositiveButtonClicked(sourceName, valuePassed);
        }
    }
}
