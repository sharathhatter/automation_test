package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ChangeCityActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onLandingPageButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                trackEvent(TrackingAware.ENTRY_PAGE_LOGIN_CLICKED, null);
                launchLogin(TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE);
                break;
            case R.id.btnRegister:
                trackEvent(TrackingAware.ENTRY_PAGE_SIGNUP_CLICKED, null);
                launchRegistrationPage();
                break;
            case R.id.btnSkip:
                // Analytics for this is done in onActivityResult of SplashActivity
                showChangeCity();
                break;
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
}