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
    private static final String TAG = DynamicScreenLoaderCallback.class.getSimpleName();
    private Context mContext;
    @Nullable
    private String mDynamicScreenType;

    public DynamicScreenLoaderCallback(Context context) {
        this.mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mDynamicScreenType = getDynamicScreenType(id);
        if (mDynamicScreenType == null) return null;
        Log.d(TAG, "onCreateLoader - Context: " + mContext + ",  id = " + mDynamicScreenType);
        String selection = DynamicPageDbHelper.COLUMN_DYNAMIC_SCREEN_TYPE + " = \'" + mDynamicScreenType
                + "\'";
        return new CursorLoader(mContext, Uri.withAppendedPath(DynamicPageDbHelper.CONTENT_URI, mDynamicScreenType),
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
        if (mDynamicScreenType == null) {
            mDynamicScreenType = getDynamicScreenType(loader.getId());
        }
        if (mDynamicScreenType == null) return; // Defensive check
        Log.d(TAG, "onLoadFinished - Context: " + mContext + ",  id = " + mDynamicScreenType);
        if (data != null && data.moveToFirst()
                && data.getBlob(DynamicPageDbHelper.COLUMN_SCREEN_DATA_INDEX) != null) {

            if (DynamicPageDbHelper.isStale(mContext, mDynamicScreenType)) {
                Log.d(TAG, "Dynamic screen = " + mDynamicScreenType + " is stale, triggering refresh");
                downloadDynamicScreen(mDynamicScreenType);
            }
            onCursorNonEmpty(data);
        } else {
            onCursorLoadingInProgress();
            downloadDynamicScreen(mDynamicScreenType);
        }
    }

    private void downloadDynamicScreen(String dynamicScreenType) {
        Class<?> cls = dynamicScreenType.equals(AbstractDynamicPageSyncService.HOME_PAGE) ?
                HomePageSyncService.class : MainMenuSyncService.class;
        Intent intent = new Intent(mContext, cls);
        mContext.startService(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset - Context: " + mContext + ",  id = " + mDynamicScreenType);
    }

    public abstract void onCursorLoadingInProgress();

    public abstract void onCursorNonEmpty(Cursor data);
}
