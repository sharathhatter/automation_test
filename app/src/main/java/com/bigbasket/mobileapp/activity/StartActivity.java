package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BrowseCategoryApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.moe.pushlibrary.MoEHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class StartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        if (!checkInternetConnection()) {
            showAlertDialogFinish(getString(R.string.deviceOfflineSmallTxt),
                    getString(R.string.deviceOffline));
            return;
        }

        MoEHelper moEHelper = new MoEHelper(this);
        moEHelper.initialize(Constants.MO_SENDER_ID, Constants.MO_APP_ID);
        moEHelper.Register(R.drawable.ic_launcher);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedCityName = preferences.getString(Constants.CITY, null);
        boolean forceRegisterDevice = getIntent().getBooleanExtra(Constants.FORCE_REGISTER_DEVICE, false);
        if (forceRegisterDevice || TextUtils.isEmpty(savedCityName)) {
            loadCities();
        } else {
            loadNavigation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.facebook.AppEventsLogger.activateApp(getCurrentActivity(), Constants.FB_APP_ID);
    }

    private void loadNavigation() {
        requestTopCategories();
    }

    private void requestTopCategories() {
        if (checkInternetConnection()) {
            final CategoryAdapter categoryAdapter = new CategoryAdapter(this);

            if (categoryAdapter.isPossiblyStale(CategoryAdapter.TOP_CATEGORY_TIMEOUT_PREF_KEY,
                    CategoryAdapter.CATEGORY_TIMEOUT_IN_MINS)) {
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
                String version = categoryAdapter.getCategoriesVersion();
                showProgressDialog(getString(R.string.please_wait));
                bigBasketApiService.browseCategory(version, new Callback<ApiResponse<BrowseCategoryApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<BrowseCategoryApiResponseContent> browseCategoryApiResponse, Response response) {
                        hideProgressDialog();
                        categoryAdapter.setLastFetchedTime(CategoryAdapter.TOP_CATEGORY_TIMEOUT_PREF_KEY);
                        BrowseCategoryApiResponseContent browseCategoryApiResponseContent =
                                browseCategoryApiResponse.apiResponseContent;
                        if (!browseCategoryApiResponseContent.aOk) {
                            categoryAdapter.insert(browseCategoryApiResponseContent.topCategoryModels,
                                    browseCategoryApiResponseContent.version);
                        }
                        loadHomePage();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        hideProgressDialog();
                        handler.handleRetrofitError(error, true);
                    }
                });
            } else {
                loadHomePage();
            }
        } else {
            handler.sendOfflineError();
        }
    }

    private void loadHomePage() {
        Intent homePageIntent = new Intent(this, BBActivity.class);
        homePageIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        startActivityForResult(homePageIntent, Constants.FORCE_REGISTER_CODE);
    }

    private void loadCities() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.listCities(new Callback<ArrayList<City>>() {
            @Override
            public void success(ArrayList<City> cities, Response response) {
                hideProgressDialog();
                renderCitySelectionDropDown(cities);
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void renderCitySelectionDropDown(final ArrayList<City> cities) {
        cities.add(0, new City(getString(R.string.chooseYourLocation), -1));
        LinearLayout layoutSpinnerAndWhyLink = (LinearLayout) findViewById(R.id.layoutSpinnerAndWhyLink);

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

        final ArrayList<String> citiesStr = new ArrayList<>();
        for(int i=1; i<cities.size(); i++){
            citiesStr.add(cities.get(i).getName());
        }
        TextView txtViewWhy = (TextView) findViewById(R.id.txtViewWhy);
        txtViewWhy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityString = UIUtil.sentenceJoin(citiesStr);
                showAlertDialog(getString(R.string.PreWhyMsg)+ " "+ cityString +". "+getString(R.string.postWhyMsg));
            }
        });
        layoutSpinnerAndWhyLink.setVisibility(View.VISIBLE);
    }

    private void doRegisterDevice(final City city) {
        String deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

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
            devicePropertiesJsonObj.put(Constants.APP_VERSION, UIUtil.getAppVersion(this));
            devicePropertiesJsonObj.put(Constants.DEVICE_MAKE, Build.MANUFACTURER);
            devicePropertiesJsonObj.put(Constants.DEVICE_MODEL, Build.MODEL);
            devicePropertiesJsonObj.put(Constants.SCREEN_RESOLUTION,
                    String.valueOf(width) + "X" + String.valueOf(height));
            devicePropertiesJsonObj.put(Constants.SCREEN_DPI, densityDpi);
        } catch (JSONException e) {
            Log.e("StartActivity", "Error while creating device-properties json");
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.registerDevice(deviceID, String.valueOf(city.getId()), devicePropertiesJsonObj.toString(),
                new Callback<RegisterDeviceResponse>() {
                    @Override
                    public void success(RegisterDeviceResponse registerDeviceResponse, Response response) {
                        hideProgressDialog();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                        SharedPreferences.Editor editor = preferences.edit();

                        String deviceID = Settings.Secure.getString(getCurrentActivity().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        editor.putString(Constants.CITY, city.getName());
                        editor.putString(Constants.CITY_ID, String.valueOf(city.getId()));
                        editor.putString(Constants.DEVICE_ID, deviceID);
                        editor.putString(Constants.VISITOR_ID_KEY, registerDeviceResponse.visitorId);
                        editor.putString(Constants.MID_KEY, null);
                        editor.putString(Constants.MEMBER_EMAIL_KEY, null);
                        editor.putString(Constants.MEMBER_FULL_NAME_KEY, null);
                        editor.commit();
                        AuthParameters.updateInstance(getCurrentActivity());
                        loadNavigation();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        hideProgressDialog();
                        handler.handleRetrofitError(error, true);
                    }
                });
    }

    @Override
    public void showProgressDialog(String msg) {
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.VISIBLE);
        LinearLayout layoutSpinnerAndWhyLink = (LinearLayout) findViewById(R.id.layoutSpinnerAndWhyLink);
        layoutSpinnerAndWhyLink.setVisibility(View.GONE);
    }

    @Override
    public void hideProgressDialog() {
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (data != null && resultCode == Constants.FORCE_REGISTER_CODE) {
            boolean forceRegisterDevice = data.getBooleanExtra(Constants.FORCE_REGISTER_DEVICE, false);
            if (forceRegisterDevice) {
                loadCities();
            }
        } else if (resultCode == NavigationCodes.GO_TO_HOME) {
            loadHomePage();
        } else {
            finish();
        }
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