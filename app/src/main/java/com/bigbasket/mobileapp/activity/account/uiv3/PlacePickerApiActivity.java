package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlacePickerApiActivity extends BackButtonActivity implements OnMapReadyCallback {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mGoogleMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMap().setMyLocationEnabled(true);
        mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        mapFragment.getMap().getUiSettings().setZoomControlsEnabled(true);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                updateLocationOnMap();
            }
        }
    }

    private void updateLocationOnMap() {
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Marker mapMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
        animateToMarker(mapMarker);
    }

    private void animateToMarker(Marker marker) {
        if (mGoogleMap == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(marker.getPosition()).zoom(12).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }
}
