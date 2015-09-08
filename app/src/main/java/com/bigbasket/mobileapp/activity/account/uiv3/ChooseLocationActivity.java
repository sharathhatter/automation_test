package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.location.LocationAutoSuggestListener;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.location.LocationAutoSuggestHelper;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class ChooseLocationActivity extends BackButtonActivity implements CityListDisplayAware,
        LocationAutoSuggestListener {

    private ArrayList<City> mCities;
    private int mSelectedIdx;
    private GoogleApiClient mGoogleApiClient;
    private View mProgressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseYourLocation));

        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        mCities = cities;
        renderLocationScreen();
    }

    private void renderLocationScreen() {
        Spinner spinnerCity = (Spinner) findViewById(R.id.spinnerCity);
        int textColor = getResources().getColor(R.color.uiv3_primary_text_color);
        BBArrayAdapter arrayAdapter = new BBArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mCities, faceRobotoRegular,
                textColor, textColor);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(arrayAdapter);
        mSelectedIdx = 0;
        buildGoogleApiClient();
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == Spinner.INVALID_POSITION) return;
                setupAreaSuggestion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setupAreaSuggestion();
        ((TextView) findViewById(R.id.lblChooseLocationDesc)).setTypeface(faceRobotoRegular);
        mProgressView = findViewById(R.id.progressBarHoriz);
    }

    private void setupAreaSuggestion() {
        AutoCompleteTextView aEditTextChooseArea = (AutoCompleteTextView)
                findViewById(R.id.aEditTextChooseArea);
        City selectedCity = mCities.get(mSelectedIdx);
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(selectedCity.getLbx(), selectedCity.getLby()),
                new LatLng(selectedCity.getUbx(), selectedCity.getUby()));
        new LocationAutoSuggestHelper<>(this, aEditTextChooseArea, mGoogleApiClient, latLngBounds, true).init();
    }

    @Override
    public void onLocationSelected(LatLng latLng) {

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
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void showProgressView() {
        if (mProgressView != null && mProgressView.getVisibility() != View.VISIBLE) {
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgressView() {
        if (mProgressView != null && mProgressView.getVisibility() != View.INVISIBLE) {
            mProgressView.setVisibility(View.INVISIBLE);
        }
    }

    public void onUseMyLocationButtonClicked(View view) {
        Intent intent = new Intent(this, PlacePickerApiActivity.class);
        startActivityForResult(intent, NavigationCodes.LOCATION_CHOSEN);
    }
}
