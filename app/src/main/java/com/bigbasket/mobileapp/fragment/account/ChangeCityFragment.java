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
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ChangeCityFragment extends BaseFragment implements PinCodeAware {

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

        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentCityName = prefer.getString("city", "");

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.listCities(new Callback<ArrayList<City>>() {
            @Override
            public void success(ArrayList<City> cities, Response response) {
                hideProgressDialog();
                displayCities(cities);
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
            }
        });
    }

    private void onCityChanged() {
        Toast.makeText(getActivity(), getResources().getString(R.string.cityChangeSuccess), Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, selectedCity.getName());
        editor.putString(Constants.CITY_ID, String.valueOf(selectedCity.getId()));
        editor.commit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(this));
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
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
                showProgressDialog(getString(R.string.please_wait));
                bigBasketApiService.changeCity(String.valueOf(selectedCity.getId()), new Callback<OldBaseApiResponse>() {
                    @Override
                    public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                        hideProgressDialog();
                        switch (oldBaseApiResponse.status) {
                            case Constants.OK:
                                onCityChanged();
                                break;
                            default:
                                showErrorMsg("Server Error");
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        hideProgressDialog();
                        showErrorMsg("Server Error");
                    }
                });
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

    @Override
    public void onPinCodeFetchSuccess() {
        finish();
    }

    @Override
    public void onPinCodeFetchFailure() {
        finish();
    }
}