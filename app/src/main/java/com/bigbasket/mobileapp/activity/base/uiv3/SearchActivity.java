package com.bigbasket.mobileapp.activity.base.uiv3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends BBActivity {
    @Nullable
    protected BBSearchableToolbarView mBbSearchableToolbarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBbSearchableToolbarView =
                (BBSearchableToolbarView) findViewById(R.id.bbSearchView);
        if (mBbSearchableToolbarView != null)
            mBbSearchableToolbarView.attachActivity(getCurrentActivity());
        if (mBbSearchableToolbarView != null) {
            mBbSearchableToolbarView.setOnSearchEventListener(new OnSearchEventListener() {

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
                showSearchUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showSearchUI() {
        if (mBbSearchableToolbarView != null) {
            mBbSearchableToolbarView.show();
        }
    }

    private void handleEancode(String eanCode) {
        logSearchEvent(eanCode);
        Intent intent = new Intent(getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
        intent.putExtra(Constants.EAN_CODE, eanCode);
        setNextScreenNavigationContext(TrackEventkeys.PS_SCAN);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    protected void doSearchByCategory(String categoryName, String categoryUrl,
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
        if (mBbSearchableToolbarView != null)
            mBbSearchableToolbarView.hide();
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.TERM, query);
        map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.PS);
        trackEvent(TrackingAware.SEARCH, map, null, null, false, true);
    }

    protected void doSearch(String searchQuery, String referrer) {
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAPTURE_CAMERA:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.CAMERA)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mBbSearchableToolbarView != null)
                        mBbSearchableToolbarView.launchScanner();
                }
                break;
            case Constants.PERMISSION_REQUEST_CODE_RECORD_AUDIO:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.RECORD_AUDIO)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mBbSearchableToolbarView != null)
                        mBbSearchableToolbarView.launchVoiceSearch();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
