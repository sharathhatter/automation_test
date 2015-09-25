package com.bigbasket.mobileapp.activity.account.uiv3;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
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
        GoogleMap.OnMyLocationButtonClickListener, LocationAutoSuggestListener {

    private GoogleApiClient mGoogleApiClient;
    private LatLng mSelectedLatLng;
    @Nullable
    private GoogleMap mGoogleMap;
    @Nullable
    private Marker mGoogleMapMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.locateYourArea));
        int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getCurrentActivity());
        switch (playServicesAvailable) {
            case ConnectionResult.SUCCESS:
                renderChooseLocation();
                break;
            default:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playServicesAvailable,
                        getCurrentActivity(), NavigationCodes.GO_TO_HOME);
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    private void renderChooseLocation() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        map.setPadding(0, 160, 0, 0);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        AutoCompleteTextView aEditTextChooseArea =
                (AutoCompleteTextView) findViewById(R.id.aEditTextChooseArea);
        new LocationAutoSuggestHelper<>(this, aEditTextChooseArea, mGoogleApiClient,
                new LatLngBounds(new LatLng(7.43231, 65.82658), new LatLng(36.93593, 99.04924)), false).init();

        TextView txtAction = (TextView) findViewById(R.id.txtAction);
        txtAction.setTypeface(faceRobotoLight);
        txtAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constants.LAT, mSelectedLatLng);
                setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED,
                        intent);
                finish();
            }
        });
    }

    @Override
    public void onLocationSelected(LatLng latLng) {
        mSelectedLatLng = latLng;
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

    private void readLastKnownLocation() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mSelectedLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
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
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mGoogleMap != null) {
            readLastKnownLocation();
        }
        return false;
    }
}
