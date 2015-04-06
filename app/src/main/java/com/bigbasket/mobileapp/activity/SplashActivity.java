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
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.CityManager;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.task.GetDynamicPageTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.analytics.FacebookEventTrackWrapper;
import com.facebook.AppEventsLogger;
import com.moe.pushlibrary.MoEHelper;
import com.newrelic.agent.android.NewRelic;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SplashActivity extends SocialLoginActivity implements DynamicScreenAware {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkInternetConnection()) {
            NewRelic.withApplicationToken(getString(R.string.new_relic_key)).start(this.getApplication());
            MoEHelper moEHelper = new MoEHelper(this);
            moEHelper.initialize(Constants.MO_SENDER_ID, Constants.MO_APP_ID);
            moEHelper.Register(R.drawable.ic_launcher);
            startSplashScreen();
        } else {
            showNoInternetConnectionView();
        }
    }

    private void startSplashScreen() {
        setContentView(R.layout.loading_layout);
        if (AuthParameters.getInstance(this).isAuthTokenEmpty() && !CityManager.hasUserChosenCity(this)) {
            if (TextUtils.isEmpty(AuthParameters.getInstance(this).getVisitorId())) {
                doRegisterDevice(new City("Bangalore", 1));
            } else {
                startLandingPage();
            }
        } else {
            loadNavigation();
        }
        trackEvent(TrackingAware.ENTRY_PAGE_SHOWN, null);
    }

    private void showNoInternetConnectionView() {
        setContentView(R.layout.layout_no_internet);

        TextView txtHeader = (TextView) findViewById(R.id.txtHeader);
        txtHeader.setVisibility(View.VISIBLE);

        ImageView imgEmptyPage = (ImageView) findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_no_internet);

        TextView txtEmptyMsg1 = (TextView) findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.lostInternetConnection);

        ImageView imgViewRetry = (ImageView) findViewById(R.id.imgViewRetry);
        imgViewRetry.setImageResource(R.drawable.empty_retry);

        imgViewRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    startSplashScreen();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FacebookEventTrackWrapper.activateApp(getCurrentActivity());
    }

    @Override
    public void onDestroy() {
        FacebookEventTrackWrapper.deactivateApp(getCurrentActivity());
        super.onDestroy();
    }

    private void loadNavigation() {
        getMainMenu();
    }

    private void getMainMenu() {
        new GetDynamicPageTask<>(this, SectionManager.MAIN_MENU, false, true, true).startTask();
    }

    @Override
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        loadHomePage();
    }

    private void loadHomePage() {
        Intent homePageIntent = new Intent(this, BBActivity.class);
        homePageIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        startActivityForResult(homePageIntent, NavigationCodes.GO_TO_HOME);
    }

    private void doRegisterDevice(final City city) {
        if (!checkInternetConnection()) {
            showNoInternetConnectionView();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));

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
            devicePropertiesJsonObj.put(Constants.APP_VERSION, DataUtil.getAppVersion(this));
            devicePropertiesJsonObj.put(Constants.DEVICE_MAKE, Build.MANUFACTURER);
            devicePropertiesJsonObj.put(Constants.DEVICE_MODEL, Build.MODEL);
            devicePropertiesJsonObj.put(Constants.SCREEN_RESOLUTION,
                    String.valueOf(width) + "X" + String.valueOf(height));
            devicePropertiesJsonObj.put(Constants.SCREEN_DPI, densityDpi);
        } catch (JSONException e) {
            Log.e("StartActivity", "Error while creating device-properties json");
        }

        bigBasketApiService.registerDevice(deviceID, String.valueOf(city.getId()), devicePropertiesJsonObj.toString(),
                new Callback<RegisterDeviceResponse>() {
                    @Override
                    public void success(RegisterDeviceResponse registerDeviceResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
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

                        startLandingPage();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error, true);
                    }
                });
    }

    private void startLandingPage() {
        Intent intent = new Intent(getCurrentActivity(), LandingPageActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void showProgressDialog(String msg) {

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.GO_TO_HOME || resultCode == NavigationCodes.CITY_CHANGED) {
            removePendingGoToHome();
            if ((data != null && data.getBooleanExtra(Constants.RELOAD_APP, false))
                    || resultCode == NavigationCodes.CITY_CHANGED) {
                loadNavigation();
            } else {
                loadHomePage();
            }
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

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onDynamicScreenFailure(RetrofitError error) {
        //showNoInternetConnectionView(); todo check with sid
        handler.handleRetrofitError(error, true);
    }

    @Override
    public void onDynamicScreenFailure(int error, String msg) {
        handler.sendEmptyMessage(error, msg, true);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.START_SCREEN;
    }
}