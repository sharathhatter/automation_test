package com.bigbasket.mobileapp.view.uiv3.search;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SearchIntentResult {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_VOICE_SEARCH, TYPE_BARCODE_SEARCH})
    public @interface Type {
    }

    public static final int TYPE_VOICE_SEARCH = 1;
    public static final int TYPE_BARCODE_SEARCH = 2;

    private String content;
    private
    @SearchIntentResult.Type
    int searchType;

    public SearchIntentResult(String content, @SearchIntentResult.Type int searchType) {
        this.content = content;
        this.searchType = searchType;
    }

    public String getContent() {
        return content;
    }

    @SearchIntentResult.Type
    public int getSearchType() {
        return searchType;
    }
}
