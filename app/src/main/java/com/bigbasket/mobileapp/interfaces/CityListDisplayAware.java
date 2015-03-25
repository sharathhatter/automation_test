package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.City;

import java.util.ArrayList;

public interface CityListDisplayAware {
    public void onReadyToDisplayCity(ArrayList<City> cities);
}
