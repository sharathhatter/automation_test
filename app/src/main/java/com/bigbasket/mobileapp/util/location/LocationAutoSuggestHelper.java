package com.bigbasket.mobileapp.util.location;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.location.LocationAutoSuggestListener;
import com.bigbasket.mobileapp.model.location.AutoCompletePlace;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class LocationAutoSuggestHelper<T extends LocationAutoSuggestListener> {
    private T ctx;
    private WeakReference<AutoCompleteTextView> autoCompleteTextViewWeakReference;
    private BBArrayAdapter<AutoCompletePlace> placeAutoSuggestAdapter;
    private AutocompleteFilter autoCompleteFilter;
    private WeakReference<GoogleApiClient> googleApiClientWeakReference;
    private LatLngBounds bounds;
    private boolean showProgress;
    private CharsetEncoder charsetEncoder;

    public LocationAutoSuggestHelper(T ctx,
                                     AutoCompleteTextView autoCompleteTextView,
                                     GoogleApiClient googleApiClient,
                                     LatLngBounds latLngBounds,
                                     boolean showProgress) {
        this.ctx = ctx;
        this.autoCompleteTextViewWeakReference = new WeakReference<>(autoCompleteTextView);
        this.googleApiClientWeakReference = new WeakReference<>(googleApiClient);
        this.bounds = latLngBounds;
        this.showProgress = showProgress;
        this.charsetEncoder = Charset.forName("US-ASCII").newEncoder();
    }

    public void init() {
        if (autoCompleteTextViewWeakReference == null ||
                autoCompleteTextViewWeakReference.get() == null) return;
        AutoCompleteTextView aEditTextChooseArea = autoCompleteTextViewWeakReference.get();
        Activity activity = ((AppOperationAware) ctx).getCurrentActivity();
        placeAutoSuggestAdapter = new BBArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                FontHolder.getInstance(activity).getFaceRobotoRegular(),
                ContextCompat.getColor(activity, R.color.uiv3_primary_text_color),
                ContextCompat.getColor(activity, R.color.uiv3_primary_text_color));
        aEditTextChooseArea.setAdapter(placeAutoSuggestAdapter);
        aEditTextChooseArea.setThreshold(2);
        aEditTextChooseArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && s.length() > 2) {
                    if (!charsetEncoder.canEncode(s)) {
                        ((AppOperationAware) ctx).getCurrentActivity().
                                showToast("Only english alphabets are allowed!");
                        return;
                    }
                    displaySuggestion(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        aEditTextChooseArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (placeAutoSuggestAdapter != null && position != AdapterView.INVALID_POSITION) {
                    AutoCompletePlace autoCompletePlace = placeAutoSuggestAdapter.getItem(position);
                    if (autoCompleteTextViewWeakReference != null && autoCompleteTextViewWeakReference.get() != null) {
                        BaseActivity.hideKeyboard((BaseActivity) ctx, autoCompleteTextViewWeakReference.get());
                    }
                    if (!TextUtils.isEmpty(autoCompletePlace.getPlaceId())) {
                        setLocationFromId(autoCompletePlace.getPlaceId());
                    }
                }
            }
        });
    }

    private void displaySuggestion(String constraint) {
        if (googleApiClientWeakReference == null || googleApiClientWeakReference.get() == null
                || !googleApiClientWeakReference.get().isConnected()) return;

        if (showProgress) {
            ((AppOperationAware) ctx).showProgressView();
        }
        if (autoCompleteFilter == null) {
            ArrayList<Integer> filterTypes = new ArrayList<>();
            filterTypes.add(Place.TYPE_GEOCODE);
            filterTypes.add(Place.TYPE_ESTABLISHMENT);
            autoCompleteFilter = AutocompleteFilter.create(filterTypes);
        }

        Places.GeoDataApi.getAutocompletePredictions(googleApiClientWeakReference.get(), constraint,
                bounds,
                autoCompleteFilter)
                .setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(AutocompletePredictionBuffer buffer) {
                        if (buffer == null) return;
                        if (buffer.getStatus().isSuccess()) {
                            placeAutoSuggestAdapter.clear();
                            for (AutocompletePrediction prediction : buffer) {
                                placeAutoSuggestAdapter.add(new AutoCompletePlace(prediction.getPlaceId(),
                                        prediction.getDescription()));
                            }
                            placeAutoSuggestAdapter.notifyDataSetChanged();
                        }
                        buffer.release();
                        if (showProgress) {
                            ((AppOperationAware) ctx).hideProgressView();
                        }
                    }
                });

    }

    private void setLocationFromId(String id) {
        if (googleApiClientWeakReference == null || googleApiClientWeakReference.get() == null
                || !googleApiClientWeakReference.get().isConnected()) return;

        if (showProgress) {
            ((AppOperationAware) ctx).showProgressView();
        }

        Places.GeoDataApi.getPlaceById(googleApiClientWeakReference.get(), id).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                    Place place = places.get(0);
                    LatLng latLng = place.getLatLng();
                    CharSequence name = place.getName();
                    places.release();
                    ctx.onLocationSelected(latLng, TextUtils.isEmpty(name) ? null : name.toString());
                } else {
                    places.release();
                }
                if (showProgress) {
                    ((AppOperationAware) ctx).hideProgressView();
                }
            }
        });
    }
}
