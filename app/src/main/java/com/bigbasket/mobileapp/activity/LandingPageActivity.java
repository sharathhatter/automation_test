package com.bigbasket.mobileapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;
import java.util.Map;


public class LandingPageActivity extends SocialLoginActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_ENTRY_SCREEN);
        setContentView(R.layout.landing_page);

        ((Button) findViewById(R.id.btnLogin)).setTypeface(faceRobotoRegular);
        ((Button) findViewById(R.id.btnRegister)).setTypeface(faceRobotoRegular);
        ((Button) findViewById(R.id.btnSkip)).setTypeface(faceRobotoRegular);
        trackEvent(TrackingAware.ENTRY_PAGE_SHOWN, null);
    }

    public void onLandingPageButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                Map<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.ENTRY_PAGE_LOGIN_CLICKED, eventAttribs);
                launchTutorial(NavigationCodes.LAUNCH_LOGIN);
                break;
            case R.id.btnRegister:
                trackEvent(TrackingAware.ENTRY_PAGE_SIGNUP_CLICKED, null);
                launchTutorial(NavigationCodes.LAUNCH_SIGNUP);
                break;
            case R.id.btnSkip:
                trackEvent(TrackingAware.ENTRY_PAGE_SKIP_BUTTON_CLICKED, null);
                launchTutorial(NavigationCodes.LAUNCH_CITY);
                break;
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
    public String getScreenTag() {
        return TrackEventkeys.LANDING_PAGE;
    }
}