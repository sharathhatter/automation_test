package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.AutoCompleteTextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.location.PlaceAutoSuggestAdapter;
import com.bigbasket.mobileapp.interfaces.PlaceAutoSuggestListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
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

import java.util.ArrayList;

public class PlacePickerApiActivity extends BackButtonActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, PlaceAutoSuggestListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    @Nullable
    private GoogleMap mGoogleMap;
    @Nullable
    private Marker mGoogleMapMarker;
    private PlaceAutoSuggestAdapter<AutoCompletePlace> mPlaceAutoSuggestAdapter;
    private ArrayList<AutoCompletePlace> mPlaces;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mPlaces = new ArrayList<>();
        mPlaceAutoSuggestAdapter =
                new PlaceAutoSuggestAdapter<>(this, android.R.layout.simple_list_item_1, mPlaces,
                        faceRobotoRegular, getResources().getColor(R.color.uiv3_primary_text_color),
                        getResources().getColor(R.color.uiv3_primary_text_color));
        aEditTextChooseArea.setAdapter(mPlaceAutoSuggestAdapter);
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
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                updateLastKnownLocationOnMap();
            }
        }
    }

    private void updateLastKnownLocationOnMap() {
        if (mGoogleMap == null) return;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mGoogleMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
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
    public void displaySuggestion(String constraint) {
        //Southwest corner to Northeast corner.
        LatLngBounds bounds = new LatLngBounds(new LatLng(7.43231, 65.82658), new LatLng(36.93593, 99.04924));
        ArrayList<Integer> filterTypes = new ArrayList<>();
        filterTypes.add(Place.TYPE_LOCALITY);
        filterTypes.add(Place.TYPE_SUBLOCALITY);
        filterTypes.add(Place.TYPE_SUBLOCALITY);
        filterTypes.add(Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_1);
        filterTypes.add(Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2);


        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, constraint, bounds,
                AutocompleteFilter.create(filterTypes))
            .setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                @Override
                public void onResult(AutocompletePredictionBuffer buffer) {
                    if (buffer == null) return;
                    if (buffer.getStatus().isSuccess()) {
                        mPlaces = new ArrayList<>();
                        for (AutocompletePrediction prediction: buffer) {
                            mPlaces.add(new AutoCompletePlace(prediction.getPlaceId(),
                                    prediction.getDescription()));
                        }
                        mPlaceAutoSuggestAdapter.notifyDataSetChanged();
                    }
                    buffer.release();
                }
            });
    }

    private static class AutoCompletePlace {
        private String placeId;
        private String description;

        public AutoCompletePlace(String placeId, String description) {
            this.placeId = placeId;
            this.description = description;
        }

        public String getPlaceId() {
            return placeId;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
