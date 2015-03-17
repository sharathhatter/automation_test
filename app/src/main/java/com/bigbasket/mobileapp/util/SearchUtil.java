package com.bigbasket.mobileapp.util;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.adapter.db.SearchSuggestionAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AutoSearchApiResponseContent;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;

import java.util.List;

public class SearchUtil {

    public static Cursor searchQueryCall(String query, Context context) {
        if (TextUtils.isEmpty(query) || query.equals(Constants.SEARCH_SUGGEST_QUERY)) {
            MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(context);
            List<MostSearchedItem> mostSearchedItems = mostSearchesAdapter.getMostSearchedItems(8);
            if (mostSearchedItems != null && mostSearchedItems.size() > 0) {
                return getMatrixCursor(mostSearchedItems);
            } else {
                return null;
            }
        } else if (query.length() < 3) {
            return null;
        }
        SearchSuggestionAdapter searchSuggestionAdapter = new SearchSuggestionAdapter(context);
        AutoSearchResponse autoSearchResponse = searchSuggestionAdapter.getStoredResponse(query);

        // If not present in local db or is older than a daywrap_content
        if (autoSearchResponse == null || autoSearchResponse.isStale()) {

            // Present in local db, but as it is stale, hence, remove it
            if (autoSearchResponse != null) {
                searchSuggestionAdapter.delete(autoSearchResponse);
                autoSearchResponse = null;
            }

            // Get the results by querying server
            if (DataUtil.isInternetAvailable(context)) {
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
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

    private static MatrixCursor getMatrixCursorForArray(String[] array, String heading, boolean isPopularSearcher) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        int startVal = 0;
        if (isPopularSearcher)
            matrixCursor.addRow(new String[]{String.valueOf(startVal++), heading, null, null, null});
        for (int i = startVal; i < array.length; i++) {
            matrixCursor.addRow(new String[]{String.valueOf(i), array[i], heading, array[i], array[i]});
        }
        return matrixCursor;
    }

    private static MatrixCursor getMatrixCursorForArray(String[] termsArray, String[] categoriesArray,
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

    private static MatrixCursor getMatrixCursor(List<MostSearchedItem> mostSearchedItems) {
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
