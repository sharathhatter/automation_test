package com.bigbasket.mobileapp.fragment.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Map;


public class ChangeCityFragment extends BaseFragment {

    private Spinner spinnerChooseCity;
    private String currentCityName;
    private City selectedCity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_change_city, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getCities();
    }

    private void getCities() {
        View base = getView();
        assert base != null;
        TextView lblChangeCity = (TextView) base.findViewById(R.id.lblChangeCity);
        lblChangeCity.setTypeface(faceRobotoRegular);

        spinnerChooseCity = (Spinner) base.findViewById(R.id.spinnerChooseCity);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_CITIES, null, false, false, null);

        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentCityName = prefer.getString("city", "");
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.GET_CITIES)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            ArrayList<City> cities = new ArrayList<>();
            for (Map.Entry<String, JsonElement> cityEntry : responseJsonObj.entrySet()) {
                String cityName = cityEntry.getKey();
                cities.add(new City(cityName, cityEntry.getValue().getAsInt(), cityName.equalsIgnoreCase(currentCityName)));
            }
            displayCities(cities);
        } else if (httpOperationResult.getUrl().contains(Constants.CHANGE_CITY)) {
            onCityChanged();
        } else if (httpOperationResult.getUrl().contains(Constants.GET_AREA_INFO)) {
            getActivity().finish();
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void onCityChanged() {
        Toast.makeText(getActivity(), getResources().getString(R.string.cityChangeSuccess), Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, selectedCity.getName());
        editor.putString(Constants.CITY_ID, String.valueOf(selectedCity.getId()));
        editor.commit();
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_AREA_INFO, null, false, false, null);
    }

    private void displayCities(final ArrayList<City> cities) {
        if (getActivity() == null) return;
        ArrayAdapter<City> citySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, cities);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChooseCity.setAdapter(citySpinnerAdapter);
        spinnerChooseCity.setSelection(City.getCurrentCityIndex(cities));
        spinnerChooseCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = cities.get(position);
                if (selectedCity.getName().equalsIgnoreCase(currentCityName)) {
                    return;
                }
                String url = MobileApiUrl.getBaseAPIUrl() + Constants.CHANGE_CITY + "?new_city_id=" + selectedCity.getId();
                startAsyncActivity(url, null, false, false, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutChangeCity) : null;
    }

    @Override
    public String getTitle() {
        return "Change City";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ChangeCityFragment.class.getName();
    }
}