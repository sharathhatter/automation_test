package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ChangeCityActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;


public class LandingPageActivity extends SocialLoginActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        ((Button) findViewById(R.id.btnLogin)).setTypeface(faceRobotoRegular);
        ((Button) findViewById(R.id.btnRegister)).setTypeface(faceRobotoRegular);
        ((Button) findViewById(R.id.btnSkip)).setTypeface(faceRobotoRegular);
        trackEvent(TrackingAware.ENTRY_PAGE_SHOWN, null);
    }

    public void onLandingPageButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                trackEvent(TrackingAware.ENTRY_PAGE_LOGIN_CLICKED, null);
                launchTutorial(NavigationCodes.LAUNCH_LOGIN);
                break;
            case R.id.btnRegister:
                trackEvent(TrackingAware.ENTRY_PAGE_SIGNUP_CLICKED, null);
                launchTutorial(NavigationCodes.LAUNCH_SIGNUP);
                break;
            case R.id.btnSkip:
                // Analytics for this is done in onActivityResult of SplashActivity
                launchTutorial(NavigationCodes.LAUNCH_CITY);
                break;
        }
    }

    private void launchTutorial(int resultCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isTutorialShown = preferences.getBoolean(Constants.TUTORIAL_SEEN, false);
        if (isTutorialShown) {
            handleTutorialResponse(resultCode);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.TUTORIAL_SEEN, true);
            editor.apply();
            Intent intent = new Intent(this, TutorialActivity.class);
            intent.putExtra(Constants.ACTION_TAB_TAG, resultCode);
            startActivityForResult(intent, NavigationCodes.TUTORIAL_SEEN);
        }
    }

    private void showChangeCity() {
        Intent intent = new Intent(this, ChangeCityActivity.class);
        startActivityForResult(intent, NavigationCodes.CITY_CHANGED);
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
    public String getScreenTag() {
        return TrackEventkeys.LANDING_PAGE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == NavigationCodes.TUTORIAL_SEEN) {
            handleTutorialResponse(resultCode);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleTutorialResponse(int resultCode) {
        switch (resultCode) {
            case NavigationCodes.LAUNCH_LOGIN:
                launchLogin(TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE);
                break;
            case NavigationCodes.LAUNCH_CITY:
                showChangeCity();
                break;
            case NavigationCodes.LAUNCH_SIGNUP:
                launchRegistrationPage();
                break;
        }
    }
}