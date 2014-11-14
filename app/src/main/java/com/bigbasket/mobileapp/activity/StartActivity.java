package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class StartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        //FlurryAgent.onStartSession(this, Constants.FLURRY_AGENT_KEY);

        if (!checkInternetConnection()) {
            showAlertDialogFinish(this, null, getString(R.string.deviceOffline));
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedCityName = preferences.getString(Constants.CITY, null);
        boolean forceRegisterDevice = getIntent().getBooleanExtra(Constants.FORCE_REGISTER_DEVICE, false);
        if (forceRegisterDevice || TextUtils.isEmpty(savedCityName)) {
            loadCities();
        } else {
            loadNavigation();
        }
    }

    private void loadNavigation() {
        requestTopCategories();
    }

    private void requestTopCategories() {
        if (checkInternetConnection()) {
            CategoryAdapter categoryAdapter = new CategoryAdapter(this);
            String version = categoryAdapter.getCategoriesVersion();
            String url = MobileApiUrl.getBaseAPIUrl() + Constants.BROWSE_CATEGORY;
            if (!TextUtils.isEmpty(version)) {
                url += "?version=" + version;
            }
            startAsyncActivity(url, null, false, AuthParameters.getInstance(this),
                    new BasicCookieStore());
        } else {
            // TODO : Add error handling
        }
    }

    private void loadHomePage() {
        Intent homePageIntent = new Intent(this, BBActivity.class);
        startActivityForResult(homePageIntent, Constants.FORCE_REGISTER_CODE);
    }

    private void loadCities() {
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.GET_CITIES;
        startAsyncActivity(url, null, false, null, new BasicCookieStore(), null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.GET_CITIES)) {

            JsonObject jsonObject = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> citiesJsonObjEntrySet = jsonObject.entrySet();
            ArrayList<City> cities = new ArrayList<>();
            cities.add(new City(getString(R.string.chooseYourLocation), -1));
            for (Map.Entry<String, JsonElement> cityObj : citiesJsonObjEntrySet) {
                cities.add(new City(cityObj.getKey(), cityObj.getValue().getAsInt()));
            }
            renderCitySelectionDropDown(cities);
        } else if (url.contains(Constants.REGISTER_DEVICE)) {
            JsonObject responseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String vid = responseJsonObj.get(Constants.VISITOR_ID).getAsString();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();

            String deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String cityName = httpOperationResult.getAdditionalCtx().get(Constants.CITY);
            String cityId = httpOperationResult.getAdditionalCtx().get(Constants.CITY_ID);
            editor.putString(Constants.CITY, cityName);
            editor.putString(Constants.CITY_ID, cityId);
            editor.putString(Constants.DEVICE_ID, deviceID);
            editor.putString(Constants.VISITOR_ID_KEY, vid);
            editor.putString(Constants.MID_KEY, null);
            editor.putString(Constants.MEMBER_EMAIL_KEY, null);
            editor.putString(Constants.MEMBER_FULL_NAME_KEY, null);
            editor.commit();
            AuthParameters.updateInstance(this);
            loadNavigation();
        } else if (url.contains(Constants.BROWSE_CATEGORY)) {
            JsonObject httpOperationJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            JsonObject responseJsonObj = httpOperationJsonObj.get(Constants.RESPONSE).getAsJsonObject();
            boolean aOk = responseJsonObj.get(Constants.A_OK).getAsBoolean();
            if (!aOk) {
                String responseVersion = responseJsonObj.get(Constants.VERSION).getAsString();
                JsonArray categoriesJsonObject = responseJsonObj.get(Constants.CATEGORIES).getAsJsonArray();

                ArrayList<TopCategoryModel> topCategoryModels =
                        ParserUtil.parseTopCategory(categoriesJsonObject);
                CategoryAdapter categoryAdapter = new CategoryAdapter(this);
                categoryAdapter.insert(topCategoryModels, responseVersion);
            }
            loadHomePage();
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void renderCitySelectionDropDown(final ArrayList<City> cities) {
        Spinner spinnerCity = (Spinner) findViewById(R.id.spinnerCity);
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        ArrayAdapter<City> citySpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(citySpinnerAdapter);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                City city = cities.get(position);
                if (city.getId() != -1) {
                    doRegisterDevice(city);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        progressBarLoading.setVisibility(View.GONE);
        spinnerCity.setVisibility(View.VISIBLE);
    }

    private void doRegisterDevice(City city) {
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.REGISTER_DEVICE;
        HashMap<Object, String> additionalCtx = new HashMap<>();
        additionalCtx.put(Constants.CITY, city.getName());
        additionalCtx.put(Constants.CITY_ID, String.valueOf(city.getId()));

        HashMap<String, String> params = new HashMap<>();
        String deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        params.put(Constants.DEVICE_ID, deviceID);
        params.put(Constants.CITY_ID, String.valueOf(city.getId()));

        // Get the screen width and height
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        JSONObject devicePropertiesJsonObj = new JSONObject();
        try {
            devicePropertiesJsonObj.put(Constants.PLATFORM, "java");
            devicePropertiesJsonObj.put(Constants.OS_NAME, "android");
            devicePropertiesJsonObj.put(Constants.OS_VERSION, Build.VERSION.RELEASE);
            devicePropertiesJsonObj.put(Constants.APP_VERSION, getAppVersion());
            devicePropertiesJsonObj.put(Constants.DEVICE_MAKE, Build.MANUFACTURER);
            devicePropertiesJsonObj.put(Constants.DEVICE_MODEL, Build.MODEL);
            devicePropertiesJsonObj.put(Constants.SCREEN_RESOLUTION,
                    String.valueOf(width) + "X" + String.valueOf(height));
            devicePropertiesJsonObj.put(Constants.SCREEN_DPI, densityDpi);
        } catch (JSONException e) {
            Log.e("StartActivity", "Error while creating device-properties json");
        }
        params.put(Constants.PROPERTIES, devicePropertiesJsonObj.toString());
        startAsyncActivity(url, params, true, null, new BasicCookieStore(), additionalCtx);
    }

    private String getAppVersion() {
        String appVersionName;
        try {
            appVersionName = getPackageManager().
                    getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = null;
        }
        return appVersionName;
    }

    @Override
    protected void showProgressDialog(String msg, String url) {
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.VISIBLE);
        Spinner spinnerCity = (Spinner) findViewById(R.id.spinnerCity);
        spinnerCity.setVisibility(View.GONE);
    }

    @Override
    protected void hideProgressDialog() {
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setActivitySuspended(false);
        if (data != null && resultCode == Constants.FORCE_REGISTER_CODE) {
            boolean forceRegisterDevice = data.getBooleanExtra(Constants.FORCE_REGISTER_DEVICE, false);
            if (forceRegisterDevice) {
                loadCities();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onHttpError() {
        // TODO : Add error handling
        showAlertDialogFinish(this, null, "Server error");
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }
}