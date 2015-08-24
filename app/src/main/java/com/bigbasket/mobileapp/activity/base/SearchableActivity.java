package com.bigbasket.mobileapp.activity.base;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.speech.RecognizerIntent;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.SearchViewAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.interfaces.SearchTermRemoveAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.SearchUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchableActivity extends BackButtonActivity
        implements SearchView.OnQueryTextListener, SearchTermRemoveAware {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ListView searchList;
    private SearchView mSearchView;
    private SearchViewAdapter mSearchListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderSearchView();
        trackEvent(TrackingAware.SEARCH_SHOWN, null);
    }

    private void renderSearchView() {
        FrameLayout contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        contentLayout.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        mSearchView = (SearchView) getLayoutInflater().inflate(R.layout.search_input_view, getToolbar(), false);
        getToolbar().addView(mSearchView);

        View searchListContainer = getLayoutInflater().inflate(R.layout.search_list, contentLayout, false);
        searchList = (ListView) searchListContainer.findViewById(R.id.searchList);

        View searchListHeaderView = getLayoutInflater().inflate(R.layout.search_list_header, searchList, false);


        RelativeLayout layoutVoice = (RelativeLayout) searchListHeaderView.findViewById(R.id.layoutVoice);
        RelativeLayout layoutScanner = (RelativeLayout) searchListHeaderView.findViewById(R.id.layoutScanner);

        TextView txtVoice = (TextView) searchListHeaderView.findViewById(R.id.txtVoice);
        TextView txtScan = (TextView) searchListHeaderView.findViewById(R.id.txtScan);
        txtVoice.setTypeface(faceRobotoRegular);
        txtScan.setTypeface(faceRobotoRegular);
        layoutVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchVoiceSearch();
            }
        });
        layoutScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanner();
            }
        });
        searchList.addHeaderView(searchListHeaderView);


        mSearchListAdapter = new SearchViewAdapter<>(getCurrentActivity(), populatePastSearchTermsList());
        searchList.setAdapter(mSearchListAdapter);
        setupSearchView();


        mSearchListAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return SearchUtil.searchQueryCall(constraint.toString(), getApplicationContext());
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null && cursor.getString(1) != null) {
                    String termType = cursor.getString(6);
                    if (!TextUtils.isEmpty(cursor.getString(3)) && cursor.getString(3).contains("/")) {
                        doSearchByCategory(cursor.getString(1), cursor.getString(3),
                                getCategorySlug(cursor.getString(3)), !TextUtils.isEmpty(termType)
                                        && termType.equals(SearchUtil.HISTORY_TERM) ? TrackEventkeys.PS_H_PL
                                        : TrackEventkeys.PS_C_PL);
                    } else {
                        if (!TextUtils.isEmpty(termType)) {
                            triggerSearch(cursor.getString(1).trim(),
                                    termType.equals(SearchUtil.HISTORY_TERM) ?
                                            TrackEventkeys.PS_H_PL : termType.equals(SearchUtil.TOP_SEARCH_TERM) ?
                                            TrackEventkeys.PS_T_PL : TrackEventkeys.PS_PL);
                        } else {
                            triggerSearch(cursor.getString(1).trim(), TrackEventkeys.PS_PL);
                        }
                    }
                }
            }
        });

        contentLayout.addView(searchListContainer);

    }

    private void doSearchByCategory(String categoryName, String categoryUrl,
                                    String categorySlug, String navigationCtx) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(categoryName, categoryUrl);

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, categorySlug));
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        setNextScreenNavigationContext(navigationCtx);
        intent.putExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        finish();
    }

    @Override
    public void notifySearchTermAdapter() {
        mSearchListAdapter.changeCursor(populatePastSearchTermsList());
        mSearchListAdapter.notifyDataSetChanged();
    }

    private void launchVoiceSearch() {
        mSearchView.clearFocus();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voicePrompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            showToast(getString(R.string.speechNotSupported));
        }
    }

    private void launchScanner() {
        mSearchView.clearFocus();
        showToast(getString(R.string.please_wait));
        new IntentIntegrator(this).initiateScan();
    }

    private void triggerSearch(String searchQuery, String referrer) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(searchQuery);
        doSearch(searchQuery, referrer);
    }

    @Override
    public void doSearch(String searchQuery, String referrer) {
        logSearchEvent(searchQuery);
        Intent data = new Intent();
        data.putExtra(Constants.SEARCH_QUERY, searchQuery);
        data.putExtra(TrackEventkeys.NAVIGATION_CTX, referrer);
        setResult(NavigationCodes.START_SEARCH, data);
        finish();
    }

    private void logSearchEvent(String query) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.TERM, query);
        map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.PS);
        trackEvent(TrackingAware.SEARCH, map, null, null, false, true);
    }

    private String getCategorySlug(String categoryUrl) {
        String[] categoryUrlArray = categoryUrl.split("/");
        return categoryUrlArray[categoryUrlArray.length - 1];
    }

    private String[] getTopSearches() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String topSearchCommaSeparatedString = preferences.getString(Constants.TOP_SEARCHES, null);
        if (topSearchCommaSeparatedString == null) return null;
        return topSearchCommaSeparatedString.split(",");
    }

    private Cursor populatePastSearchTermsList() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2});
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        int mostSearchTermsCount = mostSearchesAdapter.getRowCount();
        if (mostSearchTermsCount > 0) {
            matrixCursor.addRow(new String[]{"0", "History", null, null, null, "History", null});
            if (mostSearchTermsCount >= 5) {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(5);
                int i = 1;
                for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, mostSearchedItem.getQuery(),
                            null, SearchUtil.HISTORY_TERM});
                if (mostSearchTermsCount > 20)
                    mostSearchesAdapter.deleteFirstRow();
            } else {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(mostSearchTermsCount);
                int i = 0;
                for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            null, mostSearchedItem.getUrl(), null,
                            null, SearchUtil.HISTORY_TERM});
            }
        }
        populateTopSearch(matrixCursor);
        return matrixCursor;
    }

    private void populateTopSearch(MatrixCursor matrixCursor) {
        String[] topSearchArrayString = getTopSearches();
        if (topSearchArrayString != null && topSearchArrayString.length > 0) {
            int i = 0;
            matrixCursor.addRow(new String[]{String.valueOf(i++), "Popular Searches", null, null, null, "Popular Searches", null});
            for (String term : topSearchArrayString)
                matrixCursor.addRow(new String[]{String.valueOf(i++), term,
                        null, null, term,
                        null, SearchUtil.TOP_SEARCH_TERM});
        }
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.searchHint));

//        // icon
//        ImageView searchIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
//        searchIcon.setImageResource(R.drawable.ic_search_white_24dp);
//
//        //searchPlate.setVisibility(View.GONE);
//
//        // clear button
//        ImageView searchClose = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
//        searchClose.setImageResource(R.drawable.ic_clear_white_24dp);
//
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        if (theTextArea != null) {
            theTextArea.requestFocusFromTouch();
            theTextArea.setTextColor(Color.WHITE);
            theTextArea.setCursorVisible(true);
            theTextArea.setHintTextColor(getResources().getColor(R.color.secondary_text_default_material_dark));
        }
    }

    @Override
    protected FrameLayout getContentView() {
        return (FrameLayout) findViewById(R.id.content_frame);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        triggerSearch(query.trim(), TrackEventkeys.PS_PL);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            searchList.clearTextFilter();
        } else {
            searchList.setFilterText(newText);
            mSearchListAdapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSearchView != null) {
            hideKeyboard(this, mSearchView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> items = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (items != null && items.size() > 0) {
                String term = items.get(0).trim();
                triggerSearch(term, TrackEventkeys.PS_VOICE);
                return;
            }
        }
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String eanCode = scanResult.getContents();
            if (!TextUtils.isEmpty(eanCode)) {
                logSearchEvent(eanCode);
                Intent intent = new Intent(getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                intent.putExtra(Constants.EAN_CODE, eanCode);
                setNextScreenNavigationContext(TrackEventkeys.PS_SCAN);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
