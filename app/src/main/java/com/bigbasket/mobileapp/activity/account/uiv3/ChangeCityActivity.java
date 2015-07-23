package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.BBCheckedListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChangeCityActivity extends BackButtonActivity implements CityListDisplayAware {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_CITY_SELECTION_SCREEN);
        setTitle(getString(R.string.chooseCity));
        trackEvent(TrackingAware.CHANGE_CITY_SHOWN, null);
        loadCities();
    }

    private void loadCities() {
        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        renderCityList(cities);
    }

    private void renderCityList(final ArrayList<City> cities) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup base = (ViewGroup) inflater.inflate(R.layout.change_city, contentFrame, false);

        ((TextView) base.findViewById(R.id.txtHeaderMsg)).setTypeface(faceRobotoLight);

        final ListView listView = (ListView) base.findViewById(R.id.fabListView);
        BBCheckedListAdapter<City> citySpinnerAdapter = new BBCheckedListAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, cities);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(citySpinnerAdapter);
        listView.setItemChecked(findCurrentCityPosition(cities), true);

        FloatingActionButton btnFab = (FloatingActionButton) base.findViewById(R.id.btnFab);
        btnFab.setImageResource(R.drawable.ic_keyboard_arrow_right_white_36dp);
        btnFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = listView.getCheckedItemPosition();
                if (position > -1) {
                    City city = cities.get(position);
                    changeCity(city);
                }
            }
        });

        contentFrame.addView(base);
    }

    private int findCurrentCityPosition(ArrayList<City> cities) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentCityName = preferences.getString(Constants.CITY, null);
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getName().equals(currentCityName)) {
                return i;
            }
        }
        return 0;
    }

    protected void changeCity(final City city) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.changeCity(String.valueOf(city.getId()), new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        onCityChanged(city);
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(), oldBaseApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    private void onCityChanged(City city) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, city.getName());
        editor.putString(Constants.CITY_ID, String.valueOf(city.getId()));
        editor.putBoolean(Constants.HAS_USER_CHOSEN_CITY, true);
        editor.commit();

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.CITY, city.getName());
        trackEvent(TrackingAware.CHANGE_CITY_CLICKED, eventAttribs);
        goToHome(true);
    }
}
