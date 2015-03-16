package com.bigbasket.mobileapp.activity.base;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.SearchViewAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.adapter.db.SearchSuggestionAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AutoSearchApiResponseContent;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.List;

public class SearchableActivity extends BackButtonActivity implements SearchView.OnQueryTextListener {

    private ListView searchList;
    private SearchView mSearchView;
    private SearchViewAdapter contactListAdapter;


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
        contactListAdapter = new SearchViewAdapter<>(getCurrentActivity(), populateCursorList());
        searchList.setAdapter(contactListAdapter);
        setupSearchView();

        contentLayout.addView(base);

        contactListAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return searchQueryCall(constraint.toString());
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view.getTag()==null) return;
                doSearch(String.valueOf(view.getTag()));
            }
        });
    }

    @Override
    protected void doSearch(String searchQuery) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(searchQuery);
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SEARCH);
        intent.putExtra(Constants.SEARCH_QUERY, searchQuery);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private Cursor populateCursorList() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        int mostSearchTermsCount =mostSearchesAdapter.getRowCount();
        if(mostSearchTermsCount>0){
            if(mostSearchTermsCount>=5){
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(5);
                int i=0;
                for(MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, "SUGGESTION"});
            }else {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesAdapter.getRecentSearchedItems(mostSearchTermsCount);
                int i=0;
                for(MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, "SUGGESTION"});
            }
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
                if(mSearchView.getQuery()==null) return;
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
            contactListAdapter.getFilter().filter(newText);
            //cursor = searchQueryCall(newText);
            //contactListAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private Cursor searchQueryCall(String query) {
        if (TextUtils.isEmpty(query) || query.equals(Constants.SEARCH_SUGGEST_QUERY)) {
            MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(getCurrentActivity());
            List<MostSearchedItem> mostSearchedItems = mostSearchesAdapter.getMostSearchedItems(8);
            if (mostSearchedItems != null && mostSearchedItems.size() > 0) {
                return getMatrixCursor(mostSearchedItems);
            } else {
                return null;
            }
        } else if (query.length() < 3) {
            return null;
        }
        SearchSuggestionAdapter searchSuggestionAdapter = new SearchSuggestionAdapter(getCurrentActivity());
        AutoSearchResponse autoSearchResponse = searchSuggestionAdapter.getStoredResponse(query);

        // If not present in local db or is older than a daywrap_content
        if (autoSearchResponse == null || autoSearchResponse.isStale()) {

            // Present in local db, but as it is stale, hence, remove it
            if (autoSearchResponse != null) {
                searchSuggestionAdapter.delete(autoSearchResponse);
                autoSearchResponse = null;
            }

            // Get the results by querying server
            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
                ApiResponse<AutoSearchApiResponseContent> autoSearchApiResponse = bigBasketApiService.autoSearch(query);
                switch (autoSearchApiResponse.status) {
                    case 0:
                        autoSearchResponse = autoSearchApiResponse.apiResponseContent.autoSearchResponse;
                        searchSuggestionAdapter.insert(autoSearchResponse);
                        break;
                }
            }
        }

        MatrixCursor matrixCursor = null;
        if (autoSearchResponse != null) {
            String[] termsArray = autoSearchResponse.getTerms();
            String[] categoriesArray = autoSearchResponse.getCategories();
            String[] suggestedTermsArray = autoSearchResponse.getSuggestedTerm();
            String[] topSearchesArray = autoSearchResponse.getTopSearches();
            if ((termsArray != null && termsArray.length > 0) || (categoriesArray != null && categoriesArray.length > 0)) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getTerms(), autoSearchResponse.getCategories(),
                        autoSearchResponse.getCategoriesUrl());
            } else if (suggestedTermsArray != null && suggestedTermsArray.length > 0) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getSuggestedTerm(), "SUGGESTION", false);
            } else if (topSearchesArray != null && topSearchesArray.length > 0) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getTopSearches(), "POPULAR SEARCHES", true);
            }
        }
        return matrixCursor;
    }

    private MatrixCursor getMatrixCursorForArray(String[] array, String heading, boolean isPopularSearcher) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        int startVal= 0;
        if(isPopularSearcher)
            matrixCursor.addRow(new String[]{String.valueOf(startVal++), heading, null, null, null});
        for (int i=startVal; i < array.length; i++) {
            matrixCursor.addRow(new String[]{String.valueOf(i), array[i], heading, array[i], array[i]});
        }
        return matrixCursor;
    }

    private MatrixCursor getMatrixCursorForArray(String[] termsArray, String[] categoriesArray,
                                                 String[] categoryUrlArray) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        int i = 0;
        if (termsArray != null && termsArray.length > 0) {
            for (i = 0; i < termsArray.length; i++) {
                matrixCursor.addRow(new String[]{String.valueOf(i), termsArray[i], null, null, termsArray[i]});
            }
        }
        if (categoriesArray != null && categoriesArray.length > 0) {
            matrixCursor.addRow(new String[]{String.valueOf(i), "POPULAR CATEGORIES", null, null,
                    null});
            for (int j = 0; j < categoriesArray.length; j++) {
                String categoryName = null;
                String categoryUrl = null;
                try {
                    categoryName = categoriesArray[j];
                } catch (IndexOutOfBoundsException e) {
                }
                try {
                    categoryUrl = categoryUrlArray[j];
                } catch (IndexOutOfBoundsException e) {
                }
                if (TextUtils.isEmpty(categoryName))
                    continue;
                if (TextUtils.isEmpty(categoryUrl)) {
                    matrixCursor.addRow(new String[]{String.valueOf(i), categoryName, null, null, categoryName});
                } else {
                    matrixCursor.addRow(new String[]{String.valueOf(i), categoryName, "In Categories", categoryUrl,
                            categoryName});
                }
                i++;
            }
        }
        return matrixCursor;
    }

    private MatrixCursor getMatrixCursor(List<MostSearchedItem> mostSearchedItems) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        for (int i = 0; i < mostSearchedItems.size(); i++) {
            MostSearchedItem mostSearchedItem = mostSearchedItems.get(i);
            if (!TextUtils.isEmpty(mostSearchedItem.getUrl())) {
                matrixCursor.addRow(new String[]{String.valueOf(i), mostSearchedItem.getQuery(),
                        "In Categories", mostSearchedItem.getUrl(), mostSearchedItem.getQuery()});
            } else {
                matrixCursor.addRow(new String[]{String.valueOf(i), mostSearchedItem.getQuery(),
                        null, null, mostSearchedItem.getQuery()});
            }
        }
        return matrixCursor;
    }
}
