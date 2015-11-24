package com.bigbasket.mobileapp.activity.base.uiv3;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.search.BBSearchableToolbarView;
import com.bigbasket.mobileapp.view.uiv3.search.OnSearchEventListener;
import com.bigbasket.mobileapp.view.uiv3.search.SearchIntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SearchActivity extends BBActivity {
    @Nullable
    private BBSearchableToolbarView mBbSearchableToolbarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBbSearchableToolbarView =
                (BBSearchableToolbarView) findViewById(R.id.bbSearchView);
        if (mBbSearchableToolbarView != null) {
            mBbSearchableToolbarView.setOnSearchEventListener(new OnSearchEventListener() {
                @Override
                public void onVoiceSearchRequested() {
                    launchVoiceSearch();
                }

                @Override
                public void onBarcodeScanRequested() {
                    launchScanner();
                }

                @Override
                public void onSearchRequested(@NonNull String query) {
                    triggerSearch(query, TrackEventkeys.PS_PL);
                }

                @Override
                public void onCategorySearchRequested(String categoryName, String categoryUrl, String categorySlug) {
                    doSearchByCategory(categoryName, categoryUrl, categorySlug, TrackEventkeys.PS_C_PL);
                }
            });
        }
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (mBbSearchableToolbarView != null) {
                    mBbSearchableToolbarView.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voicePrompt));
        try {
            startActivityForResult(intent, BBSearchableToolbarView.REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            showToast(getString(R.string.speechNotSupported));
        }
    }

    private void launchScanner() {
        showToast(getString(R.string.please_wait));
        new IntentIntegrator(this).initiateScan();
    }

    private void handleEancode(String eanCode) {
        logSearchEvent(eanCode);
        Intent intent = new Intent(getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
        intent.putExtra(Constants.EAN_CODE, eanCode);
        setNextScreenNavigationContext(TrackEventkeys.PS_SCAN);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private void doSearchByCategory(String categoryName, String categoryUrl,
                                    String categorySlug, String navigationCtx) {
        MostSearchesDbHelper mostSearchesDbHelper = new MostSearchesDbHelper(this);
        mostSearchesDbHelper.update(categoryName, categoryUrl);

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, categorySlug));
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        setNextScreenNavigationContext(navigationCtx);
        intent.putExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private void triggerSearch(String searchQuery, String referrer) {
        MostSearchesDbHelper mostSearchesDbHelper = new MostSearchesDbHelper(this);
        mostSearchesDbHelper.update(searchQuery);
        doSearch(searchQuery, referrer);
    }

    private void logSearchEvent(String query) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.TERM, query);
        map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.PS);
        trackEvent(TrackingAware.SEARCH, map, null, null, false, true);
    }

    public void doSearch(String searchQuery, String referrer) {
        logSearchEvent(searchQuery);
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery.trim()));
        intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        intent.putExtra(Constants.TITLE, searchQuery);
        setNextScreenNavigationContext(referrer);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SearchIntentResult searchIntentResult = BBSearchableToolbarView.parseActivityResult(requestCode, resultCode, data);
        if (searchIntentResult != null) {
            switch (searchIntentResult.getSearchType()) {
                case SearchIntentResult.TYPE_BARCODE_SEARCH:
                    handleEancode(searchIntentResult.getContent());
                    return;
                case SearchIntentResult.TYPE_VOICE_SEARCH:
                    triggerSearch(searchIntentResult.getContent(), TrackEventkeys.PS_VOICE);
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mBbSearchableToolbarView != null && mBbSearchableToolbarView.onBackPressed()) return;
        super.onBackPressed();
    }
}
