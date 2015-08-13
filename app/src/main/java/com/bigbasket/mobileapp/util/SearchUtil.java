package com.bigbasket.mobileapp.util;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.SearchSuggestionAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AutoSearchApiResponseContent;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;

public class SearchUtil {

    //public static final String SEARCH_LEFT_ICON = "s";
    public static final String CROSS_ICON = "c";
    public static final String HISTORY_LEFT_ICON = "h";

    public static Cursor searchQueryCall(String query, Context context) {
        if (TextUtils.isEmpty(query.trim()) || (query.trim().length() < 3)) return null;

        SearchSuggestionAdapter searchSuggestionAdapter = new SearchSuggestionAdapter(context);
        AutoSearchResponse autoSearchResponse = searchSuggestionAdapter.getStoredResponse(query);

        // If not present in local db or is older than a day
        if (autoSearchResponse == null || autoSearchResponse.isStale()) {

            // Get the results by querying server
            if (DataUtil.isInternetAvailable(context)) {
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
                ApiResponse<AutoSearchApiResponseContent> autoSearchApiResponse = bigBasketApiService.autoSearch(query);
                switch (autoSearchApiResponse.status) {
                    case 0:
                        autoSearchResponse = autoSearchApiResponse.apiResponseContent.autoSearchResponse;
                        searchSuggestionAdapter.insertAsync(autoSearchResponse);
                        break;
                }
            }
        }

        MatrixCursor matrixCursor = null;
        if (autoSearchResponse != null) {
            String[] termsArray = autoSearchResponse.getTerms();
            String[] categoriesArray = autoSearchResponse.getCategories();
            String[] suggestedTermsArray = autoSearchResponse.getSuggestedTerm();
            //String[] topSearchesArray = autoSearchResponse.getTopSearches();
            if ((termsArray != null && termsArray.length > 0) || (categoriesArray != null && categoriesArray.length > 0)) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getTerms(), autoSearchResponse.getCategories(),
                        autoSearchResponse.getCategoriesUrl());
            } else if (suggestedTermsArray != null && suggestedTermsArray.length > 0) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getSuggestedTerm(), "Suggestion", false);
            }
        }
        if (matrixCursor == null) {
            // When no products found so that top-searches can be shown
            matrixCursor = instantiateMatrixCursor();
        }
        populateTopSearch(matrixCursor, context);
        return matrixCursor;
    }


    private static void populateTopSearch(MatrixCursor matrixCursor, Context context) {
        String[] topSearchArrayString = getTopSearches(context);
        if (topSearchArrayString != null && topSearchArrayString.length > 0) {
            int i = 0;
            matrixCursor.addRow(new String[]{String.valueOf(i++), "Popular Searches", null, null, null, "Popular Searches", null});
            for (String term : topSearchArrayString)
                matrixCursor.addRow(new String[]{String.valueOf(i++), term,
                        null, null, null,
                        null, null});
        }
    }


    private static String[] getTopSearches(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String topSearchCommaSeparatedString = preferences.getString(Constants.TOP_SEARCHES, null);
        if (topSearchCommaSeparatedString == null) return null;
        return topSearchCommaSeparatedString.split(",");
    }

    private static MatrixCursor getMatrixCursorForArray(String[] array, String heading, boolean isPopularSearcher) {
        MatrixCursor matrixCursor = instantiateMatrixCursor();
        //last two column are for left and right icon for a row
        int startVal = 0;
        if (isPopularSearcher)
            matrixCursor.addRow(new String[]{String.valueOf(startVal++), heading, null, null, null, heading, null});
        for (int i = startVal; i < array.length; i++) {
            matrixCursor.addRow(new String[]{String.valueOf(i), array[i], null, array[i], array[i],
                    null, null});
        }
        return matrixCursor;
    }

    private static MatrixCursor instantiateMatrixCursor() {
        return new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2});
    }

    private static MatrixCursor getMatrixCursorForArray(String[] termsArray, String[] categoriesArray,
                                                        String[] categoryUrlArray) {
        MatrixCursor matrixCursor = instantiateMatrixCursor();
        int i = 0;
        if (termsArray != null && termsArray.length > 0) {
            for (i = 0; i < termsArray.length; i++) {
                matrixCursor.addRow(new String[]{String.valueOf(i), termsArray[i], null, null, termsArray[i],
                        null, null});
            }
        }
        if (categoriesArray != null && categoriesArray.length > 0) {
            matrixCursor.addRow(new String[]{String.valueOf(i), "Popular Categories", null, null,
                    null, "Popular Categories", null});
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
                    matrixCursor.addRow(new String[]{String.valueOf(i), categoryName, null, null, null,
                            null, null});
                } else {
                    matrixCursor.addRow(new String[]{String.valueOf(i), categoryName, null, categoryUrl,
                            null, null, null});
                }
                i++;
            }
        }
        return matrixCursor;
    }
}
