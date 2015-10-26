package com.bigbasket.mobileapp.interfaces.location;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public interface LocationAutoSuggestListener {
    void onLocationSelected(LatLng latLng, @Nullable String areaName);
}
