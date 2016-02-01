package com.bigbasket.mobileapp.activity;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.FlatPageFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.communicationhub.AlertsOffersScreenFragment;
import com.bigbasket.mobileapp.fragment.communicationhub.AskUsFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

public class CommunicationHubActivity extends BaseActivity {

    private boolean isFaqToBeShown = false;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentStatePagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_communication_hub);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }


        if (getIntent().getExtras() != null)
            isFaqToBeShown = getIntent().getBooleanExtra(Constants.COMMUNICATION_HUB_FAQ_SHOW, false);
        /**
         * setting the tabs
         * 1. Alerts: displays all the alerts like order status,wallet funding etc
         * 2. Offers: displays all the offers available for the user.(can be fill image or if image is unaviable it will be replaced with title and description)
         * TODO :description of tab 3
         */
        setUpTabs();
    }

    private void setUpTabs() {

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.content_frame);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), isFaqToBeShown);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Setup the Tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.slidingTabs);
        // By using this method the tabs will be populated according to viewPager's count and
        // with the name from the pagerAdapter getPageTitle()
        tabLayout.setTabsFromPagerAdapter(mSectionsPagerAdapter);
        // This method ensures that tab selection events update the ViewPager and page changes update the selected tab.
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                HashMap<String, String> attrs = new HashMap<>(1);
                attrs.put(TrackEventkeys.NAVIGATION_CTX, tab.getText().toString());
                trackEvent(COMMUNICATION_HUB_TAB_CHANGED, attrs);
            }
        });

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
        return getString(R.string.title_activity_communication_hub);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_communication_hub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private final Context mContext;
        private boolean isFaqToBeShown;

        public SectionsPagerAdapter(Context context, FragmentManager fm, boolean isFaqToBeShown) {
            super(fm);
            mContext = context;
            this.isFaqToBeShown = isFaqToBeShown;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Bundle args = new Bundle();
            switch (position) {
                case 0:
                    if (isFaqToBeShown) {
                        args.putString(Constants.WEBVIEW_URL, Constants.COMMUNICATION_HUB_FAQ_URL);
                        args.putString(Constants.WEBVIEW_TITLE, mContext.getResources()
                                .getString(R.string.title_activity_communication_hub));
                        FlatPageFragment flatPageFragment = new FlatPageFragment();
                        flatPageFragment.setArguments(args);
                        return flatPageFragment;
                    } else {
                        /**
                         * sending filter as general for the alerts in bundle
                         */
                        args.putString(Constants.COMMUNICATION_HUB_FILTER, Constants.COMMUNICATION_HUB_ALERT);
                        return AlertsOffersScreenFragment.newInstance(args);
                    }
                case 1:
                    if (isFaqToBeShown) {
                        return new AskUsFragment();
                    } else {
                        /**
                         * sending filter as offers for the offers in bundle
                         */
                        args.putString(Constants.COMMUNICATION_HUB_FILTER, Constants.COMMUNICATION_HUB_OFFER);
                        return AlertsOffersScreenFragment.newInstance(args);
                    }
                case 2:
                    return new AskUsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            if (isFaqToBeShown)
                return 2;
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if (isFaqToBeShown)
                        return mContext.getString(R.string.communication_hub_tab_faq);
                    return mContext.getString(R.string.communication_hub_tab_alerts);
                case 1:
                    if (isFaqToBeShown)
                        return mContext.getString(R.string.communication_hub_tab_askme);
                    return mContext.getString(R.string.communication_hub_tab_offer);
                case 2:
                    return mContext.getString(R.string.communication_hub_tab_askme);
            }
            return null;
        }
    }

}
