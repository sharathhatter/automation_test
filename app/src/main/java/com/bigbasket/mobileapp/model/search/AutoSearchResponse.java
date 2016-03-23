package com.bigbasket.mobileapp.model.search;

import android.database.Cursor;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.SearchSuggestionDbHelper;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AutoSearchResponse {

    public static final int CACHE_INTERVAL_IN_DAYS = 7;

    private String query;

    @SerializedName(Constants.TERM)
    private String[] terms;

    private String[] categories;

    @SerializedName(Constants.CATEGORIES_URL)
    private String[] categoriesUrl;

    @SerializedName(Constants.SUGGESTED_TERM)
    private String[] suggestedTerm;

//    @SerializedName(Constants.TOP_SEARCHES)
//    private String[] topSearches;

    private String createdOn;

    public static final String[] PROJECTION = new String[]{
            SearchSuggestionDbHelper.QUERY,
            SearchSuggestionDbHelper.CREATED_ON,
            SearchSuggestionDbHelper.TERMS,
            SearchSuggestionDbHelper.CATEGORIES,
            SearchSuggestionDbHelper.CATEGORY_URLS,
            SearchSuggestionDbHelper.SUGGESTED_TERMS};
    public static final int COLUMN_INDEX_QUERY = 0;
    public static final int COLUMN_INDEX_CREATED_ON = COLUMN_INDEX_QUERY + 1;
    public static final int COLUMN_INDEX_TERMS = COLUMN_INDEX_CREATED_ON + 1;
    public static final int COLUMN_INDEX_CATEGORIES = COLUMN_INDEX_TERMS + 1;
    public static final int COLUMN_INDEX_CATEGORY_URLS = COLUMN_INDEX_CATEGORIES + 1;
    public static final int COLUMN_INDEX_SUGGESTED_TERMS = COLUMN_INDEX_CATEGORY_URLS + 1;

    public AutoSearchResponse(Cursor cursor) {
        this.query = cursor.getString(COLUMN_INDEX_QUERY);
        String termsStr = cursor.getString(COLUMN_INDEX_CREATED_ON);
        String categoriesStr = cursor.getString(COLUMN_INDEX_TERMS);
        String categoriesUrlStr = cursor.getString(COLUMN_INDEX_CATEGORIES);
        String suggestedTermsStr = cursor.getString(COLUMN_INDEX_SUGGESTED_TERMS);
        this.terms = TextUtils.isEmpty(termsStr) ? null : termsStr.split(",");
        this.categories = TextUtils.isEmpty(categoriesStr) ? null : categoriesStr.split(",");
        this.categoriesUrl = TextUtils.isEmpty(categoriesUrlStr) ? null : categoriesUrlStr.split(",");
        this.suggestedTerm = TextUtils.isEmpty(suggestedTermsStr) ? null : suggestedTermsStr.split(",");
        this.createdOn = cursor.getString(cursor.getColumnIndex(SearchSuggestionDbHelper.CREATED_ON));
    }

    public String getQuery() {
        return query;
    }

    public String[] getTerms() {
        return terms;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getCategoriesUrl() {
        return categoriesUrl;
    }

    public String[] getSuggestedTerm() {
        return suggestedTerm;
    }

    public boolean isStale() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date createdOnDate = dateFormat.parse(createdOn);
            Date now = new Date();
            long days = TimeUnit.DAYS.convert(now.getTime() - createdOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return days > CACHE_INTERVAL_IN_DAYS;
        } catch (ParseException e) {
            return false;
        }
    }
}
