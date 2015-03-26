package com.bigbasket.mobileapp.activity.base;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.speech.RecognizerIntent;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.SearchViewAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.SearchUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchableActivity extends BackButtonActivity implements SearchView.OnQueryTextListener {

    private ListView searchList;
    private SearchView mSearchView;
    private SearchViewAdapter mSearchListAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderSearchView();
    }

    private void renderSearchView() {
        FrameLayout contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        getToolbarLayout().setVisibility(View.GONE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.search_list, contentLayout, false);

        searchList = (ListView) base.findViewById(R.id.searchList);
        mSearchView = (SearchView) base.findViewById(R.id.search_view);
        ImageView imgBckBtn = (ImageView) base.findViewById(R.id.imgBckBtn);
        imgBckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentActivity().finish();
            }
        });

        View searchListHeaderView = getLayoutInflater().inflate(R.layout.search_list_header, searchList, false);
        TextView txtVoice = (TextView) searchListHeaderView.findViewById(R.id.txtVoice);
        TextView txtScan = (TextView) searchListHeaderView.findViewById(R.id.txtScan);
        txtVoice.setTypeface(faceRobotoRegular);
        txtScan.setTypeface(faceRobotoRegular);
        txtVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchVoiceSearch();
            }
        });
        txtScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanner();
            }
        });
        searchList.addHeaderView(searchListHeaderView);

        mSearchListAdapter = new SearchViewAdapter<>(getCurrentActivity(), populateCursorList());
        searchList.setAdapter(mSearchListAdapter);
        setupSearchView();

        contentLayout.addView(base);

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
                if (cursor != null && cursor.getString(4) != null) {
                    doSearch(cursor.getString(1));
                }
            }
        });
    }

    private void launchVoiceSearch() {
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
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    public void doSearch(String searchQuery) {
        Intent data = new Intent();
        data.putExtra(Constants.SEARCH_QUERY, searchQuery);
        setResult(FragmentCodes.START_SEARCH, data);
        finish();
    }

    private String[] getTopSearches(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String topSearchCommaSeparatedString = preferences.getString(Constants.TOP_SEARCHES, null);
        if(topSearchCommaSeparatedString ==null) return null;
        return topSearchCommaSeparatedString.split(",");
    }

    private Cursor populateCursorList() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        int mostSearchTermsCount = mostSearchesAdapter.getRowCount();
        if (mostSearchTermsCount > 0) {
            if (mostSearchTermsCount >= 5) {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(5);
                int i = 0;
                for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, "SUGGESTION"});
            } else {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(mostSearchTermsCount);
                int i = 0;
                for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, "SUGGESTION"});
            }
        }

        String[] topSearchArrayString = getTopSearches();
        if (topSearchArrayString != null && topSearchArrayString.length > 0) {
            int i = 0;
            matrixCursor.addRow(new String[]{String.valueOf(i++), "POPULAR SEARCHES", null, null, null});
            for (String term : topSearchArrayString)
                matrixCursor.addRow(new String[]{String.valueOf(i++), term,
                        null, null, "POPULAR SEARCHES"});
        }

        return matrixCursor;
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        //mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint("Search for products");
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchView.getQuery() == null) return;
                doSearch(String.valueOf(mSearchView.getQuery()));
            }
        });
    }

    @Override
    protected FrameLayout getContentView() {
        return (FrameLayout) findViewById(R.id.content_frame);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            searchList.clearTextFilter();
        } else {
            searchList.setFilterText(newText);
            mSearchListAdapter.getFilter().filter(newText);
            //cursor = searchQueryCall(newText);
            //mSearchListAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> items = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (items != null && items.size() > 0) {
                doSearch(items.get(0));
                return;
            }
        }
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String skuId = scanResult.getContents();
            if (!TextUtils.isEmpty(skuId)) {
                Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                intent.putExtra(Constants.SKU_ID, skuId);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

}
