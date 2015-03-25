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
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.facebook.AppEventsLogger;


public class LandingPageActivity extends SocialLoginActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        setUpSocialButtons((Button) findViewById(R.id.plus_sign_in_button),
                (Button) findViewById(R.id.btnFBLogin));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    public void onLandingPageButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                launchLogin(TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE);
                break;
            case R.id.btnRegister:
                break;
            case R.id.btnSkip:
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