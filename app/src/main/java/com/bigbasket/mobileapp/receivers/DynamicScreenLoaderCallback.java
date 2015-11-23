package com.bigbasket.mobileapp.receivers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.bigbasket.mobileapp.service.HomePageSyncService;
import com.bigbasket.mobileapp.service.MainMenuSyncService;
import com.bigbasket.mobileapp.util.LoaderIds;

public abstract class DynamicScreenLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DynamicScreenLoaderCallback.class.getName();
    private Context context;
    private String dynamicScreenType;

    public DynamicScreenLoaderCallback(Context context) {
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        dynamicScreenType = getDynamicScreenType(id);
        if (dynamicScreenType == null) return null;
        Log.d(TAG, "Create loader invoked for = " + dynamicScreenType);
        String selection = DynamicPageDbHelper.COLUMN_DYNAMIC_SCREEN_TYPE + " = \'" + dynamicScreenType
                + "\'";
        return new CursorLoader(context, Uri.withAppendedPath(DynamicPageDbHelper.CONTENT_URI, dynamicScreenType),
                DynamicPageDbHelper.getDefaultProjection(),
                selection,
                null, null);
    }

    @Nullable
    private String getDynamicScreenType(int loaderID) {
        switch (loaderID) {
            case LoaderIds.HOME_PAGE_ID:
                return AbstractDynamicPageSyncService.HOME_PAGE;
            case LoaderIds.MAIN_MENU_ID:
                return AbstractDynamicPageSyncService.MAIN_MENU;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Load finished invoked for = " + dynamicScreenType);
        if (data != null && data.moveToFirst()) {
            if (DynamicPageDbHelper.isStale(context, dynamicScreenType)) {
                Log.d(TAG, "Dynamic screen = " + dynamicScreenType + " is stale, triggering refresh");
                downloadDynamicScreen();
            }
            onCursorNonEmpty(data);
        } else {
            onCursorLoadingInProgress();
            downloadDynamicScreen();
        }
    }

    private void downloadDynamicScreen() {
        Class<?> cls = dynamicScreenType.equals(AbstractDynamicPageSyncService.HOME_PAGE) ?
                HomePageSyncService.class : MainMenuSyncService.class;
        Intent intent = new Intent(context, cls);
        context.startService(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public abstract void onCursorLoadingInProgress();

    public abstract void onCursorNonEmpty(Cursor data);
}
