package com.bigbasket.mobileapp.devconfig;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.SplashActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class BBDevConfigActivity extends SocialLoginActivity
        implements OnConfigChangeListener {
    String mNewServerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        setupActionBar();
        showServerPreferencesFragment(savedInstanceState);
    }

    @Override
    protected void onPause() {
        moEHelper.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        moEHelper.onResume(this);
        super.onResume();
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {
        setTitle(title);
    }

    @Override
    public String getScreenTag() {
        return getClass().getSimpleName();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showServerPreferencesFragment(Bundle savedInstanceState) {
        mNewServerName = null;
        if (savedInstanceState == null) {
            ServerPreferencesFragment fragment = new ServerPreferencesFragment();
            getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServerChanged(String newServer) {
        mNewServerName = newServer;
        onLogoutRequested();
    }

    @Override
    public void onHttpLoggingLevelChanged(String newValue) {
        BigBasketApiAdapter.reset();
        DeveloperConfigs.saveHttpLoggingLevel(getApplicationContext(), newValue);
    }

    @Override
    protected void postLogout(boolean success) {
        super.postLogout(success);
        if (success) {

            OkHttpClient httpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                            DeveloperConfigs.getHttpLoggingInterceptor(getApplicationContext()))
                    .build();
            BigBasketApiService bbService = new Retrofit.Builder()
                    .baseUrl(mNewServerName + MobileApiUrl.API_PATH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build()
                    .create(BigBasketApiService.class);
            bbService.listCities().enqueue(new Callback<ArrayList<City>>() {
                @Override
                public void onResponse(Call<ArrayList<City>> call,
                                       Response<ArrayList<City>> response) {
                    if (call != null && call.isCanceled()) return;
                    hideProgressDialog();
                    if(response.isSuccess()) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                .remove(Constants.VISITOR_ID_KEY).commit();
                        CityManager.clearChosenCity(getApplicationContext());
                        AuthParameters.reset();
                        DeveloperConfigs.saveMapiServerAddress(getApplicationContext(), mNewServerName);
                        Intent intent = new Intent(getCurrentActivity(), SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(getScreenTag(), response.code() + ": " + response.message());
                        if(response.code() == 401) {
                            Toast.makeText(getCurrentActivity(), "Unknown Protocol, Change the server domain to: "
                                    + (mNewServerName.startsWith("https") ?
                                            "http" + mNewServerName.substring(5) :
                                            "https" + mNewServerName.substring(4)),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getCurrentActivity(), response.message(), Toast.LENGTH_LONG).show();
                        }
                        showServerPreferencesFragment(null);
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<City>> call, Throwable t) {
                    if (call != null && call.isCanceled()) return;
                    hideProgressDialog();
                    Log.e(getScreenTag(), "Error", t);
                    Toast.makeText(getCurrentActivity(), "Invalid server address: " + t.toString(),
                            Toast.LENGTH_LONG).show();
                    showServerPreferencesFragment(null);
                }
            });

        } else {
            showServerPreferencesFragment(null);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ServerPreferencesFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private String mSavedServerAddress;
        private String mSavedHttpLogLevel;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(DeveloperConfigs.DEVELOPER_PREF_FILE);
            addPreferencesFromResource(R.xml.pref_server_config);
            setHasOptionsMenu(true);
            mSavedServerAddress = DeveloperConfigs.getMapiServerAddress(getActivity());
            mSavedHttpLogLevel = String.valueOf(DeveloperConfigs.getHttpLoggingLevel(getActivity()));

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(DeveloperConfigs.MAPI_SERVER_ADDRESS),
                    mSavedServerAddress);
            bindPreferenceSummaryToValue(findPreference(DeveloperConfigs.HTTP_LOGGING_LEVEL),
                    mSavedHttpLogLevel);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         */
        private void bindPreferenceSummaryToValue(Preference preference, Object defaultValue) {
            if(preference == null){
                return;
            }
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            Context context = preference.getContext();
            // Trigger the listener immediately with the preference's
            // current value.
            onPreferenceChange(preference, defaultValue);

        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            if (preference.getKey() != null && getActivity() instanceof OnConfigChangeListener) {
                switch (preference.getKey()) {
                    case DeveloperConfigs.MAPI_SERVER_ADDRESS:
                        //Logout and reload application
                        if ((mSavedServerAddress == null ||
                                !mSavedServerAddress.equalsIgnoreCase(stringValue))) {
                            ((OnConfigChangeListener) getActivity()).onServerChanged(stringValue);
                        }
                        return false;
                    case DeveloperConfigs.HTTP_LOGGING_LEVEL:
                        if ((!mSavedHttpLogLevel.equalsIgnoreCase(stringValue))) {
                            ((OnConfigChangeListener) getActivity()).onHttpLoggingLevelChanged(stringValue);
                            mSavedHttpLogLevel = stringValue;
                        }
                        return false;
                }
            }
            return true;
        }
    }
}

