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
import android.widget.Toast;

import com.bigbasket.mobileapp.adapter.db.DynamicScreenAdapter;
import com.bigbasket.mobileapp.service.DynamicScreenSyncService;

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
        Toast.makeText(context, "Create loader invoked for = " + dynamicScreenType, Toast.LENGTH_SHORT).show();
        String selection = DynamicScreenAdapter.COLUMN_DYNAMIC_SCREEN_TYPE + " = \'" + dynamicScreenType
                + "\'";
        return new CursorLoader(context, Uri.withAppendedPath(DynamicScreenAdapter.CONTENT_URI, dynamicScreenType),
                DynamicScreenAdapter.getDefaultProjection(),
                selection,
                null, null);
    }

    @Nullable
    private String getDynamicScreenType(int loaderID) {
        switch (loaderID) {
            case DynamicScreenSyncService.HOME_PAGE_ID:
                return DynamicScreenSyncService.HOME_PAGE;
            case DynamicScreenSyncService.MAIN_MENU_ID:
                return DynamicScreenSyncService.MAIN_MENU;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Toast.makeText(context, "Load finished invoked for = " + dynamicScreenType, Toast.LENGTH_SHORT).show();
        if (data != null && data.moveToFirst()) {
            if (DynamicScreenAdapter.isStale(context, dynamicScreenType)) {
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
        Intent intent = new Intent(context, DynamicScreenSyncService.class);
        intent.setAction(dynamicScreenType);
        context.startService(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public abstract void onCursorLoadingInProgress();

    public abstract void onCursorNonEmpty(Cursor data);
}
