package com.bigbasket.mobileapp.activity.account.uiv3;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.location.LocationAutoSuggestListener;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.location.LocationAutoSuggestHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlacePickerApiActivity extends BackButtonActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, LocationAutoSuggestListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LatLng mSelectedLatLng;
    private String mAreaName;
    @Nullable
    private GoogleMap mGoogleMap;
    @Nullable
    private Marker mGoogleMapMarker;
    private AutoCompleteTextView mEditTextChooseArea;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseDeliveryLocation));
        int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCurrentActivity());
        switch (playServicesAvailable) {
            case ConnectionResult.SUCCESS:
                renderChooseLocation(savedInstanceState);
                break;
            default:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playServicesAvailable,
                        getCurrentActivity(), NavigationCodes.GO_TO_HOME);
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    private void renderChooseLocation(@Nullable Bundle savedInstanceState) {
        buildGoogleApiClient();

        mEditTextChooseArea =
                (AutoCompleteTextView) findViewById(R.id.aEditTextChooseArea);
        new LocationAutoSuggestHelper<>(this, mEditTextChooseArea, mGoogleApiClient,
                new LatLngBounds(new LatLng(7.43231, 65.82658), new LatLng(36.93593, 99.04924)), false).init();

        if (savedInstanceState == null) {
            String mediaState = Environment.getExternalStorageState();
            boolean hasStorage = !(mediaState == null || mediaState.equalsIgnoreCase(Environment.MEDIA_REMOVED)
                    || mediaState.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)
                    || mediaState.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE)
                    || mediaState.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED));
            if (hasStorage) {
                // Map fragment crashes if the device doesn't have sd-card or
                // a sd-card simulation storage area (e.g. Devices like Nexus don't have sd-card slot,
                // but they still simulate it so that apps continue to work).
                // Many Android One phones don't simulate this storage.
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.layout_map, mapFragment)
                        .commit();
                //map.setPadding(0, 160, 0, 0);
                mapFragment.getMapAsync(this);

                boolean isAttached = mEditTextChooseArea.post(new Runnable() {
                    @Override
                    public void run() {
                        setUpMyLocationButtonUI(true);
                    }
                });
                if (!isAttached) {
                    setUpMyLocationButtonUI(false);
                }
            }
        }

        TextView txtAction = (TextView) findViewById(R.id.txtAction);
        txtAction.setTypeface(faceRobotoLight);
        txtAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedLatLng == null) {
                    showAlertDialog(getString(R.string.location_not_found));
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.LAT, mSelectedLatLng);
                intent.putExtra(Constants.AREA, mAreaName);
                setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED,
                        intent);
                finish();
            }
        });
    }

    private void setUpMyLocationButtonUI(boolean isAttached) {
        if (isSuspended()) return;
        if (mGoogleMap == null) return;
        int myLocPaddingTop;
        if (isAttached && mEditTextChooseArea.getLayoutParams() != null
                && mEditTextChooseArea.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {

            myLocPaddingTop = ((ViewGroup.MarginLayoutParams) mEditTextChooseArea.getLayoutParams()).topMargin;
            myLocPaddingTop += mEditTextChooseArea.getHeight() + (int) getResources().getDimension(R.dimen.padding_normal);
            if (myLocPaddingTop < 0) {
                myLocPaddingTop = 160;
            }
        } else {
            myLocPaddingTop = 160;
        }
        mGoogleMap.setPadding(0, myLocPaddingTop, 0, 0);
    }

    @Override
    public void onLocationSelected(LatLng latLng, @Nullable String name) {
        mSelectedLatLng = latLng;
        mAreaName = name;
        updateLastKnownLocationOnMap();
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
        return R.layout.uiv3_pick_location;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseActivity.hideKeyboard(this, mEditTextChooseArea);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        readLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void readLastKnownLocation() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mSelectedLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mAreaName = null;
                updateLastKnownLocationOnMap();
            }
        }
    }

    private void updateLastKnownLocationOnMap() {
        if (mGoogleMap == null) return;
        if (mGoogleMapMarker != null) {
            mGoogleMapMarker.remove();
        }
        mGoogleMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(mSelectedLatLng)
                .draggable(true));
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mGoogleMapMarker = marker;
                mSelectedLatLng = marker.getPosition();
            }
        });
        animateToMarker();
    }

    private void animateToMarker() {
        if (mGoogleMap == null || mGoogleMapMarker == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mGoogleMapMarker.getPosition()).zoom(13).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED);
        super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        setUpMyLocationButtonUI(true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mGoogleMap != null) {
            readLastKnownLocation();
        }
        return false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
