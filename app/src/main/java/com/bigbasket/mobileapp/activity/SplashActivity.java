package com.bigbasket.mobileapp.activity;

import android.content.ActivityNotFoundException;
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
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.RegisterDeviceResponse;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.devconfig.DevConfigViewHandler;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;


public class SplashActivity extends BaseActivity implements AppOperationAware {

    private boolean mIsFromActivityResult;
    private ImageView imgBBLogo;
    private View landingView;
    private View progressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NC_SPLASH_SCREEN);

        // Defensive fix
        removePendingCodes();
        try {
            boolean isHDFCPayMode = getIntent().getBooleanExtra(Constants.MODE_HDFC_PAY, false);
            if (isHDFCPayMode) {
                HDFCPayzappHandler.setHDFCPayMode(this);
            }
        } catch (ClassCastException e) {
            // Fail silently
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (savedInstanceState == null) {
            if (preferences.contains(Constants.FIRSE_TIME_USER)) {
                MoEngageWrapper.setExistingUser(moEHelper, true);
            } else {
                MoEngageWrapper.setExistingUser(moEHelper, false);
                editor.putBoolean(Constants.FIRSE_TIME_USER, true);
            }
        }
        if (!BuildConfig.DEBUG) {
            AppsFlyerLib.setAppsFlyerKey(Constants.APP_FLYER_ID);
            AppsFlyerLib.setUseHTTPFalback(true);
            AppsFlyerLib.sendTracking(getApplicationContext()); //detects installation, session and updates
            AppsFlyerLib.setCurrencyCode("INR");
            editor.putBoolean(Constants.APP_LAUNCH, true);
        }
        editor.commit(); //HomeActivity need APP_LAUNCH flag, so we don't want to write data in background
    }

    private void startSplashScreen() {
        setContentView(R.layout.splash_screen_layout);
        imgBBLogo = (ImageView) findViewById(R.id.imgBBLogo);
        DevConfigViewHandler.setView(imgBBLogo);
        UIUtil.displayAsyncImage(imgBBLogo, R.drawable.bb_splash_logo, true);
        landingView = findViewById(R.id.layoutLoginButtons);
        progressView = findViewById(R.id.progressBar);

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
        if (BuildConfig.DEBUG) {
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
    protected void onStart() {
        super.onStart();
        if (imgBBLogo != null) {
            UIUtil.displayAsyncImage(imgBBLogo, R.drawable.bb_splash_logo, true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (imgBBLogo != null) {
            imgBBLogo.setImageBitmap(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadHomePage() {
        Intent homePageIntent = new Intent(this, HomeActivity.class);
        homePageIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        startActivityForResult(homePageIntent, NavigationCodes.GO_TO_HOME);
        finish();
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

        String imei = UIUtil.getUniqueDeviceIdentifier(this);
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

                BaseApplication.updateGAUserId(getCurrentActivity(),
                        registerDeviceResponse.visitorId);

                editor.commit();
                AuthParameters.reset();

                /**
                 * checking if the activity was launched from the DeeplinkDispatcherActivity
                 * if redirectIntent is not null the same intent is fired again i.e the calling intent, while finishing the current activity
                 */
                Intent redirectIntent = getIntent().getParcelableExtra(Constants.REDIRECT_INTENT);
                if (redirectIntent != null) {
                    try {
                        startActivity(redirectIntent);
                        finish();
                        return;
                    } catch (ActivityNotFoundException ex) {

                    }
                }
                startLandingPage();

            }

            @Override
            public void onFailure(Call<RegisterDeviceResponse> call, Throwable t) {
                if (isSuspended() || (call != null && call.isCanceled())) return;
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
        progressView.setVisibility(View.GONE);
        landingView.setVisibility(View.VISIBLE);
    }

    public void onLandingPageButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                Map<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
                trackEvent(TrackingAware.ENTRY_PAGE_LOGIN_CLICKED, eventAttribs);
                launchLogin(TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE, true);
                break;
            case R.id.btnRegister:
                trackEvent(TrackingAware.ENTRY_PAGE_SIGNUP_CLICKED, null);
                launchRegistrationPage();
                break;
            case R.id.btnSkip:
                trackEvent(TrackingAware.ENTRY_PAGE_SKIP_BUTTON_CLICKED, null);
                showChangeCity(true, TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE, false);
                break;
        }
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
            handleResults();
        } else if (resultCode != NavigationCodes.RESULT_SIGN_UP_CANCELLED
                && resultCode != NavigationCodes.RESULT_SIGN_IN_CANCELLED
                && resultCode != NavigationCodes.RESULT_CHANGE_CITY_CANCELLED) {
            finish();
        }
    }

    private void handleResults() {
        loadHomePage();
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
    public String getScreenTag() {
        return TrackEventkeys.START_SCREEN;
    }
}