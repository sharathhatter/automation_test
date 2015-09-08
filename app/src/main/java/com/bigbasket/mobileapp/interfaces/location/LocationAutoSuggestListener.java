package com.bigbasket.mobileapp.interfaces.location;

import com.google.android.gms.maps.model.LatLng;

public interface LocationAutoSuggestListener {
    void onLocationSelected(LatLng latLng);
}
