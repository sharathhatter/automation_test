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

import com.appsflyer.AppsFlyerLib;
import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.devconfig.DevConfigViewHandler;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.newrelic.agent.android.NewRelic;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Call;


public class SplashActivity extends SocialLoginActivity implements DynamicScreenAware, AppOperationAware {

    private boolean mIsFromActivityResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_SPLASH_SCREEN);
        boolean reloadApp = getIntent().getBooleanExtra(Constants.RELOAD_APP, false);
        if (reloadApp) {
            setContentView(R.layout.loading_layout);
            ImageView imgBBLogo = (ImageView) findViewById(R.id.imgBBLogo);
            UIUtil.displayAsyncImage(imgBBLogo, R.drawable.bb_splash_logo);

            mIsFromActivityResult = true;
            handleResults(true);
        } else {
            AppsFlyerLib.setAppsFlyerKey(Constants.APP_FLYER_ID);
            AppsFlyerLib.setUseHTTPFalback(true);
            AppsFlyerLib.sendTracking(getApplicationContext()); //detects installation, session and updates
            AppsFlyerLib.setCurrencyCode("INR");

            // Defensive fix
            removePendingCodes();
            try {
                boolean isHDFCPayMode = getIntent().getBooleanExtra(Constants.MODE_HDFC_PAY, false);
                if (isHDFCPayMode) {
                    HDFCPayzappHandler.setHDFCPayMode(this);
                }
            } catch (ClassCastException e) {

            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            if (preferences.contains(Constants.FIRSE_TIME_USER)) {
                MoEngageWrapper.setExistingUser(moEHelper, true);
            } else {
                MoEngageWrapper.setExistingUser(moEHelper, false);
                editor.putBoolean(Constants.FIRSE_TIME_USER, true);
            }
            if (!BuildConfig.DEBUG) {
                editor.putBoolean(Constants.APP_LAUNCH, true);
                if (checkInternetConnection()) {
                    NewRelic.withApplicationToken(getString(R.string.new_relic_key)).start(this.getApplication());
                }
            }
            editor.commit(); //HomeActivity need APP_LAUNCH flag, so we don't want to write data in background
        }
    }

    private void startSplashScreen() {
        setContentView(R.layout.loading_layout);
        ImageView imgBBLogo = (ImageView) findViewById(R.id.imgBBLogo);
        UIUtil.displayAsyncImage(imgBBLogo, R.drawable.bb_splash_logo);

        if (AuthParameters.getInstance(this).isAuthTokenEmpty() && !CityManager.hasUserChosenCity(this)) {
            if (TextUtils.isEmpty(AuthParameters.getInstance(this).getVisitorId())) {
                doRegisterDevice(new City("Bangalore", 1));
            } else {
                startLandingPage();
            }
        } else {
            loadHomePage();
        }
    }

    private void showNoInternetConnectionView(String msg) {
        setContentView(R.layout.layout_no_internet);

        TextView txtHeader = (TextView) findViewById(R.id.txtHeader);
        txtHeader.setVisibility(View.VISIBLE);

        ImageView imgEmptyPage = (ImageView) findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_no_internet);
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DevConfigViewHandler.setView(imgEmptyPage);
        }

        TextView txtEmptyMsg1 = (TextView) findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(msg);

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
        if (mIsFromActivityResult) return;
        if (checkInternetConnection()) {
            startSplashScreen();
        } else {
            showNoInternetConnectionView(getString(R.string.lostInternetConnection));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        loadHomePage();
    }

    private void loadHomePage() {
        launchTutorial(FragmentCodes.START_HOME);
    }

    @Override
    protected void handleTutorialResponse(int resultCode) {
        if (resultCode == FragmentCodes.START_HOME) {
            Intent homePageIntent = new Intent(this, HomeActivity.class);
            homePageIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
            startActivityForResult(homePageIntent, NavigationCodes.GO_TO_HOME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                finish();
            }
        } else {
            super.handleTutorialResponse(resultCode);
        }
    }

    private void doRegisterDevice(final City city) {
        if (!checkInternetConnection()) {
            showNoInternetConnectionView(getString(R.string.lostInternetConnection));
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

        String imei = UIUtil.getIMEI(this);
        Call<RegisterDeviceResponse> call = bigBasketApiService.registerDevice(imei, deviceID,
                String.valueOf(city.getId()), devicePropertiesJsonObj.toString());
        call.enqueue(new BBNetworkCallback<RegisterDeviceResponse>(this, true) {
            @Override
            public void onSuccess(RegisterDeviceResponse registerDeviceResponse) {
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
                AuthParameters.reset();

                startLandingPage();
            }

            @Override
            public void onFailure(Throwable t) {
                showNoInternetConnectionView(getString(R.string.networkError));
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
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
        mIsFromActivityResult = true;
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            removePendingGoToHome();
            boolean reloadApp = data != null && data.getBooleanExtra(Constants.RELOAD_APP, false);
            handleResults(reloadApp);
        } else if (requestCode == NavigationCodes.TUTORIAL_SEEN) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            finish();
        }
    }

    private void handleResults(boolean reloadApp) {
        removePendingGoToHome();
        if (reloadApp) {
            loadHomePage();
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
    }

    @Override
    public void onDynamicScreenFailure(Throwable t) {
        handler.handleRetrofitError(t, true);
    }

    @Override
    public void onDynamicScreenFailure(int error, String msg) {
        handler.sendEmptyMessage(error, msg, true);
    }

    @Override
    public void onDynamicScreenHttpFailure(int error, String msg) {
        handler.handleHttpError(error, msg, true);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.START_SCREEN;
    }
}