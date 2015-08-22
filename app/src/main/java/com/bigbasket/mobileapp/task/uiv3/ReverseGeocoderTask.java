package com.bigbasket.mobileapp.task.uiv3;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.location.OnAddressFetchedListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

public class ReverseGeocoderTask<T> extends AsyncTask<LatLng, Void, List<Address>> {

    private WeakReference<T> ctxRef;

    public ReverseGeocoderTask(T ctx) {
        this.ctxRef = new WeakReference<>(ctx);
    }

    @Override
    protected void onPreExecute() {
        if (ctxRef.get() != null && !((CancelableAware) ctxRef.get()).isSuspended()) {
            ((ProgressIndicationAware) ctxRef.get()).showProgressDialog("Getting address...");
        }
    }

    @Override
    protected List<Address> doInBackground(LatLng... params) {
        List<Address> addresses = null;
        try {
            LatLng latLng = params[0];
            Geocoder geocoder = new Geocoder(((ActivityAware) ctxRef.get()).getCurrentActivity(),
                    Locale.getDefault());
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException | IllegalArgumentException e) {

        }
        return addresses;
    }

    @Override
    protected void onPostExecute(@Nullable List<Address> addresses) {
        if (ctxRef.get() != null && !((CancelableAware) ctxRef.get()).isSuspended()) {
            ((ProgressIndicationAware) ctxRef.get()).hideProgressDialog();
            ((OnAddressFetchedListener) ctxRef.get()).onAddressFetched(addresses);
        }
    }
}
