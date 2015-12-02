package com.bigbasket.mobileapp.view.uiv3.search;

import android.support.annotation.NonNull;

public interface OnSearchEventListener {

    void onSearchRequested(@NonNull String query);

    void onCategorySearchRequested(String categoryName, String categoryUrl, String categorySlug);
}