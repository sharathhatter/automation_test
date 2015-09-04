package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.location.LocationAutoSuggestListener;
import com.bigbasket.mobileapp.interfaces.location.OnAddressFetchedListener;
import com.bigbasket.mobileapp.task.uiv3.ReverseGeocoderTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.location.LocationAutoSuggestHelper;
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

import java.util.List;

public class PlacePickerApiActivity extends BackButtonActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, OnAddressFetchedListener,
        LocationAutoSuggestListener {

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

        renderChooseLocation();
        if (!DataUtil.isLocationServiceEnabled(this)) {
            showAlertDialog(getString(R.string.enableLocationHeading),
                    getString(R.string.enableLocation),
                    DialogButton.YES, DialogButton.CANCEL, Constants.LOCATION, null,
                    getString(R.string.enable));
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
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        AutoCompleteTextView aEditTextChooseArea =
                (AutoCompleteTextView) findViewById(R.id.aEditTextChooseArea);
        new LocationAutoSuggestHelper<>(this, aEditTextChooseArea, mGoogleApiClient,
                new LatLngBounds(new LatLng(7.43231, 65.82658), new LatLng(36.93593, 99.04924)), false).init();
        ViewGroup layoutChooseLocation = (ViewGroup) findViewById(R.id.layoutChooseLocation);
        UIUtil.setUpFooterButton(this, layoutChooseLocation, null, "Continue", true);
        layoutChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ReverseGeocoderTask<>(getCurrentActivity()).execute(mSelectedLatLng);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mGoogleMap != null && mGoogleMapMarker != null) {
            mGoogleMapMarker.remove();
            updateLastKnownLocationOnMap();
        }
        return false;
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

    @Override
    public void onAddressFetched(@Nullable List<Address> addresses) {
        if (addresses != null && addresses.size() > 0) {
            showToast(addresses.get(0).getPostalCode() + ":" +
                    addresses.get(0).getLocality());
        }
    }
}
