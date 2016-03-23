package com.bigbasket.mobileapp.view.uiv3.search;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.SearchUtil;

import java.util.List;

/**
 * Created by muniraju on 31/12/15.
 */
public class SearchHistoryLoader extends AsyncTaskLoader<Cursor> {

    private Cursor mCursor;

    public SearchHistoryLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2});
        List<MostSearchedItem> mostSearchedItemList =
                MostSearchesDbHelper.getRecentSearchedItems(getContext(), 5);
        if (mostSearchedItemList != null && !mostSearchedItemList.isEmpty()) {
            matrixCursor.addRow(new String[]{"0", getContext().getString(R.string.history), null,
                    null, null, getContext().getString(R.string.history), null});
            int i = 0;
            for (MostSearchedItem mostSearchedItem : mostSearchedItemList) {
                matrixCursor.addRow(new String[]{String.valueOf(++i), mostSearchedItem.getQuery(),
                        mostSearchedItem.getUrl(), null, mostSearchedItem.getQuery(),
                        null, SearchUtil.HISTORY_TERM});
            }
        }
        SearchUtil.populateTopSearch(matrixCursor, getContext());
        return matrixCursor;
    }

    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
