package com.bigbasket.mobileapp.view.uiv3.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.SearchViewAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesDbHelper;
import com.bigbasket.mobileapp.interfaces.OnSearchTermActionCallback;
import com.bigbasket.mobileapp.model.search.MostSearchedItem;
import com.bigbasket.mobileapp.util.SearchUtil;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BBSearchableToolbarView extends LinearLayout implements OnSearchTermActionCallback {
    public static final int REQ_CODE_SPEECH_INPUT = 100;

    private ListView mSearchList;
    private EditText mSearchView;
    private SearchViewAdapter mSearchListAdapter;
    private OnSearchEventListenerProxy mOnSearchEventListenerProxy;
    private View mImgVoice;
    private View mImgBarcode;
    private View mImgClear;
    private Activity mAttachedActivity;

    public BBSearchableToolbarView(Context context) {
        super(context);
        init();
    }

    public BBSearchableToolbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void attachActivity(Activity activity) {
        mAttachedActivity = activity;
    }

    public void show() {
        setListAdapter();
        this.setVisibility(VISIBLE);
        mSearchView.requestFocus();
        UIUtil.changeStatusBarColor(getContext(), R.color.primary_dark_material_light);
        BaseActivity.showKeyboard(mSearchView);
    }

    public void hide() {
        mOnSearchEventListenerProxy.reset();
    }

    private void init() {
        setOrientation(VERTICAL);
        setBackgroundColor(0x88000000);
        setClickable(true);
        OnClickListener hideViewOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        };
        setOnClickListener(hideViewOnClickListener);

        View.inflate(getContext(), R.layout.bb_searchable_toolbar_layout, this);
        mSearchView = (EditText) findViewById(R.id.searchView);
        mSearchList = (ListView) findViewById(R.id.searchList);
        mImgVoice = findViewById(R.id.imgVoice);
        mImgBarcode = findViewById(R.id.imgBarcode);
        mImgClear = findViewById(R.id.imgClear);

        Toolbar toolbarSearch = (Toolbar) findViewById(R.id.toolbarSearch);
        toolbarSearch.setNavigationIcon(R.drawable.ic_arrow_back_grey600_24dp);
        toolbarSearch.setNavigationOnClickListener(hideViewOnClickListener);

        mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnSearchEventListenerProxy == null) return;
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null && cursor.getString(1) != null) {
                    if (!TextUtils.isEmpty(cursor.getString(3)) && cursor.getString(3).contains("/")) {
                        mOnSearchEventListenerProxy.onCategorySearchRequested(cursor.getString(1), cursor.getString(3),
                                getCategorySlug(cursor.getString(3)));
                    } else {
                        mOnSearchEventListenerProxy.onSearchRequested(cursor.getString(1).trim());
                    }
                }
            }
        });

        setupSearchView();
        mImgVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchVoiceSearch();
            }
        });
        mImgBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanner();
            }
        });
        mImgClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setText("");
            }
        });
        mOnSearchEventListenerProxy = new OnSearchEventListenerProxy(this);
    }

    private void setListAdapter() {
        mSearchListAdapter = new SearchViewAdapter<>(getContext(), populatePastSearchTermsList(), this);
        mSearchListAdapter.setFilterQueryProvider(new SearchViewAdapter.SearchFilterQueryProvider(getContext()));
        mSearchList.setAdapter(mSearchListAdapter);
    }

    private void setupSearchView() {
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    onQueryTextSubmit(mSearchView.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onQueryTextChange(s != null ? s.toString() : null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private Cursor populatePastSearchTermsList() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2});
        MostSearchesDbHelper mostSearchesDbHelper = new MostSearchesDbHelper(getContext());
        int mostSearchTermsCount = mostSearchesDbHelper.getRowCount();
        if (mostSearchTermsCount > 0) {
            matrixCursor.addRow(new String[]{"0", getContext().getString(R.string.history), null,
                    null, null, getContext().getString(R.string.history), null});
            if (mostSearchTermsCount >= 5) {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesDbHelper.getRecentSearchedItems(5);
                int i = 1;
                for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                    matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                            mostSearchedItem.getUrl(), null, mostSearchedItem.getQuery(),
                            null, SearchUtil.HISTORY_TERM});
                if (mostSearchTermsCount > 20)
                    mostSearchesDbHelper.deleteFirstRow();
            } else {
                List<MostSearchedItem> mostSearchedItemList = mostSearchesDbHelper.getRecentSearchedItems(mostSearchTermsCount);
                if (mostSearchedItemList != null) {
                    int i = 0;
                    for (MostSearchedItem mostSearchedItem : mostSearchedItemList)
                        matrixCursor.addRow(new String[]{String.valueOf(i++), mostSearchedItem.getQuery(),
                                null, mostSearchedItem.getUrl(), null,
                                null, SearchUtil.HISTORY_TERM});
                }
            }
        }
        SearchUtil.populateTopSearch(matrixCursor, getContext());
        return matrixCursor;
    }

    public void setOnSearchEventListener(OnSearchEventListener searchEventListener) {
        this.mOnSearchEventListenerProxy.setOnSearchEventListener(searchEventListener);
    }

    private void onQueryTextSubmit(String query) {
        if (mOnSearchEventListenerProxy != null && !TextUtils.isEmpty(query)) {
            mOnSearchEventListenerProxy.onSearchRequested(query.trim());
        }
    }

    private void onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mSearchList.clearTextFilter();
            if (mImgBarcode != null) {
                mImgBarcode.setVisibility(View.VISIBLE);
            }
            if (mImgVoice != null) {
                mImgVoice.setVisibility(View.VISIBLE);
            }
            if (mImgClear != null && mImgClear.getVisibility() != View.GONE) {
                mImgClear.setVisibility(View.GONE);
            }
        } else {
            if (mImgBarcode != null) {
                mImgBarcode.setVisibility(View.GONE);
            }
            if (mImgVoice != null) {
                mImgVoice.setVisibility(View.GONE);
            }
            if (mImgClear != null && mImgClear.getVisibility() != View.VISIBLE) {
                mImgClear.setVisibility(View.VISIBLE);
            }
            if (mSearchList != null && mSearchListAdapter != null &&
                    mSearchListAdapter.getFilter() != null) {
                mSearchList.setFilterText(newText);
                mSearchListAdapter.getFilter().filter(newText);
            }
        }
    }

    private String getCategorySlug(String categoryUrl) {
        String[] categoryUrlArray = categoryUrl.split("/");
        return categoryUrlArray[categoryUrlArray.length - 1];
    }

    @Override
    public void onSearchTermDeleted() {
        if (mSearchListAdapter != null) {
            mSearchListAdapter.changeCursor(populatePastSearchTermsList());
            mSearchListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSearchText(String term) {
        mSearchView.setText("");
        mSearchView.append(term); // This also moves the screen cursor to the end
    }

    private static class OnSearchEventListenerProxy implements OnSearchEventListener {
        private OnSearchEventListener mOnSearchEventListener;
        private BBSearchableToolbarView bbSearchableToolbarView;

        public OnSearchEventListenerProxy(BBSearchableToolbarView bbSearchableToolbarView) {
            this.bbSearchableToolbarView = bbSearchableToolbarView;
        }

        private void setOnSearchEventListener(OnSearchEventListener onSearchEventListener) {
            this.mOnSearchEventListener = onSearchEventListener;
        }

        private void reset() {
            if (bbSearchableToolbarView == null) return;
            if (bbSearchableToolbarView.mSearchView != null) {
                bbSearchableToolbarView.mSearchView.clearFocus();
            }
            if (bbSearchableToolbarView.mSearchListAdapter != null) {
                bbSearchableToolbarView.mSearchListAdapter = null;
            }
            if (bbSearchableToolbarView.mSearchList != null) {
                bbSearchableToolbarView.mSearchList.setAdapter(null);
            }
            if (bbSearchableToolbarView != null) {
                UIUtil.changeStatusBarColor(bbSearchableToolbarView.getContext(), R.color.uiv3_status_bar_background);
                BaseActivity.hideKeyboard(bbSearchableToolbarView.getContext(), bbSearchableToolbarView.mSearchView);
                bbSearchableToolbarView.setVisibility(View.GONE);
            }
            if (bbSearchableToolbarView != null && bbSearchableToolbarView.mSearchView != null) {
                bbSearchableToolbarView.mSearchView.setText("");
            }
        }

        @Override
        public void onSearchRequested(@NonNull String query) {
            reset();
            if (mOnSearchEventListener != null) {
                mOnSearchEventListener.onSearchRequested(query);
            }
        }

        @Override
        public void onCategorySearchRequested(String categoryName, String categoryUrl, String categorySlug) {
            reset();
            if (mOnSearchEventListener != null) {
                mOnSearchEventListener.onCategorySearchRequested(categoryName, categoryUrl, categorySlug);
            }
        }
    }

    private void launchVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mAttachedActivity.getString(R.string.voicePrompt));
        try {
            mAttachedActivity.startActivityForResult(intent, BBSearchableToolbarView.REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.speechNotSupported), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchScanner() {
        Toast.makeText(getContext(), getContext().getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
        new IntentIntegrator(mAttachedActivity).initiateScan();
    }

    @Nullable
    public static SearchIntentResult parseActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> items = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (items != null && items.size() > 0) {
                return new SearchIntentResult(items.get(0).trim(), SearchIntentResult.TYPE_VOICE_SEARCH);
            }
        }
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String eanCode = scanResult.getContents();
            if (!TextUtils.isEmpty(eanCode)) {
                return new SearchIntentResult(eanCode, SearchIntentResult.TYPE_BARCODE_SEARCH);
            }
        }
        return null;
    }

    public boolean onBackPressed() {
        if (getVisibility() == View.VISIBLE) {
            hide();
            return true;
        }
        return false;
    }
}
