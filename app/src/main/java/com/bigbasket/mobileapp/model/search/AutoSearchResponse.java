package com.bigbasket.mobileapp.model.search;

import android.database.Cursor;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.SearchSuggestionAdapter;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @SerializedName(Constants.TOP_SEARCHES)
    private String[] topSearches;

    private String createdOn;

    public AutoSearchResponse(Cursor cursor) {
        this.query = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_QUERY));
        String termsStr = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_TERMS));
        String categoriesStr = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_CATEGORIES));
        String categoriesUrlStr = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_CATEGORY_URLS));
        String suggestedTermsStr = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_SUGGESTED_TERMS));
        this.terms = TextUtils.isEmpty(termsStr) ? null : termsStr.split(",");
        this.categories = TextUtils.isEmpty(categoriesStr) ? null : categoriesStr.split(",");
        this.categoriesUrl = TextUtils.isEmpty(categoriesUrlStr) ? null : categoriesUrlStr.split(",");
        this.suggestedTerm = TextUtils.isEmpty(suggestedTermsStr) ? null : suggestedTermsStr.split(",");
        this.createdOn = cursor.getString(cursor.getColumnIndex(SearchSuggestionAdapter.COLUMN_CREATED_ON));
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

    public String[] getTopSearches() {
        return topSearches;
    }

    public boolean isStale() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
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
