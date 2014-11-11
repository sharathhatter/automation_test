package com.bigbasket.mobileapp.contentProvider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.adapter.db.SearchSuggestionAdapter;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;
import java.util.List;

public class SearchSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment().toLowerCase().trim();
        if (TextUtils.isEmpty(query) || query.equals(Constants.SEARCH_SUGGEST_QUERY)) {
            MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(getContext());
            List<MostSearchedItem> mostSearchedItems = mostSearchesAdapter.getMostSearchedItems(8);
            if (mostSearchedItems != null && mostSearchedItems.size() > 0) {
                return getMatrixCursor(mostSearchedItems);
            } else {
                return null;
            }
        } else if (query.length() < 3) {
            return null;
        }
        SearchSuggestionAdapter searchSuggestionAdapter = new SearchSuggestionAdapter(getContext());
        AutoSearchResponse autoSearchResponse = searchSuggestionAdapter.getStoredResponse(query);

        // If not present in local db or is older than a day
        if (autoSearchResponse == null || autoSearchResponse.isStale()) {

            // Present in local db, but as it is stale, hence, remove it
            if (autoSearchResponse != null) {
                searchSuggestionAdapter.delete(autoSearchResponse);
                autoSearchResponse = null;
            }

            // Get the results by querying server
            String autoSearchUrl = MobileApiUrl.getBaseAPIUrl() + Constants.AUTO_SEARCH_URL;
            HashMap<String, String> params = new HashMap<>();
            params.put("t", query);
            AuthParameters authParameters = AuthParameters.getInstance(getContext());
            HttpRequestData httpRequestData = new HttpRequestData(autoSearchUrl, params, false,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), null);
            HttpOperationResult httpOperationResult = DataUtil.doHttpGet(httpRequestData);
            if (httpOperationResult.getResponseCode() == HttpStatus.SC_OK ||
                    httpOperationResult.getResponseCode() == HttpStatus.SC_ACCEPTED) {
                JsonObject httpResponseJsonObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
                int status = httpResponseJsonObj.get(Constants.STATUS).getAsInt();
                switch (status) {
                    case 0:
                        JsonObject responseObj = httpResponseJsonObj.get(Constants.RESPONSE).getAsJsonObject();
                        JsonObject termsJsonObj = responseObj.get(Constants.TC).getAsJsonObject();
                        autoSearchResponse = ParserUtil.parseAutoSearchResponse(termsJsonObj);
                        searchSuggestionAdapter.insert(autoSearchResponse);
                        break;
                    default:
                        break;
                }
            }
        }

        MatrixCursor matrixCursor = null;
        if (autoSearchResponse != null) {
            if (!ArrayUtils.isEmpty(autoSearchResponse.getTerms()) || !ArrayUtils.isEmpty(autoSearchResponse.getCategories())) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getTerms(), autoSearchResponse.getCategories(),
                        autoSearchResponse.getCategoriesUrl());
            } else if (!ArrayUtils.isEmpty(autoSearchResponse.getSuggestedTerm())) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getSuggestedTerm(), "Suggestion");
            } else if (!ArrayUtils.isEmpty(autoSearchResponse.getTopSearches())) {
                matrixCursor = getMatrixCursorForArray(autoSearchResponse.getTopSearches(), "Top Searches");
            }
        }
        return matrixCursor;
    }

    private MatrixCursor getMatrixCursorForArray(String[] array, String heading) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        for (int i = 0; i < array.length; i++) {
            matrixCursor.addRow(new String[]{String.valueOf(i), array[i], heading, array[i]});
        }
        return matrixCursor;
    }

    private MatrixCursor getMatrixCursorForArray(String[] termsArray, String[] categoriesArray,
                                                 String[] categoryUrlArray) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
        int i = 0;
        if (!ArrayUtils.isEmpty(termsArray)) {
            for (i = 0; i < termsArray.length; i++) {
                matrixCursor.addRow(new String[]{String.valueOf(i), termsArray[i], null, null, termsArray[i]});
            }
        }
        if (!ArrayUtils.isEmpty(categoriesArray)) {
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

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
