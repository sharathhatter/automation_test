package com.bigbasket.mobileapp.activity.account.uiv3;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LocateOnMapActivity extends BackButtonActivity implements OnMapReadyCallback {

    private Address mAddress;
    private Marker mGoogleMapMarker;
    private GoogleMap mGoogleMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAddress = getIntent().getParcelableExtra(Constants.UPDATE_ADDRESS);

        setTitle(getString(R.string.locateOnMap));

        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_locate_on_map, null);
        contentView.addView(base);

        TextView txtMemberAddress = (TextView) base.findViewById(R.id.txtMemberAddress);
        TextView txtMemberName = (TextView) base.findViewById(R.id.txtMemberName);
        txtMemberAddress.setTypeface(faceRobotoRegular);
        txtMemberName.setTypeface(faceRobotoRegular);
        txtMemberName.setText(mAddress.getName());
        txtMemberAddress.setText(mAddress.toString());

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layoutLocateOnMapFragment, supportMapFragment);
        ft.commit();
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng memberLatLng = new LatLng(mAddress.getLatitude(), mAddress.getLongitude());
        mGoogleMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(memberLatLng)
                .title(TextUtils.isEmpty(mAddress.getAddressNickName()) ?
                        mAddress.getName() : mAddress.getAddressNickName())
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
//        mGoogleMap.setMyLocationEnabled(true);
//        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        animateToMarker();
    }

    private void animateToMarker() {
        if (mGoogleMap == null || mGoogleMapMarker == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mGoogleMapMarker.getPosition()).zoom(12).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void onLocateMemberButtonClicked(View v) {
        if (mGoogleMapMarker == null) return;
        LatLng markerPosition = mGoogleMapMarker.getPosition();
        if (markerPosition.latitude == mAddress.getLatitude() &&
                markerPosition.longitude == mAddress.getLongitude()) {
            finish();
        } else {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog("Updating location...");
            bigBasketApiService.updateLocation(mAddress.getId(), markerPosition.latitude, markerPosition.longitude,
                    new Callback<BaseApiResponse>() {
                        @Override
                        public void success(BaseApiResponse updateLocationApiResponse, Response response) {
                            try {
                                hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            switch (updateLocationApiResponse.status) {
                                case 0:
                                    showToast(updateLocationApiResponse.message);
                                    setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED);
                                    finish();
                                    break;
                                default:
                                    handler.sendEmptyMessage(updateLocationApiResponse.status,
                                            updateLocationApiResponse.message);
                                    break;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            try {
                                hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            handler.handleRetrofitError(error);
                        }
                    });
        }
    }
}
