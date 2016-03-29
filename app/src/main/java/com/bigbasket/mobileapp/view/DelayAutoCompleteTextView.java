package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.adapter.AddressAutoCompleteAdapter;
import com.crashlytics.android.Crashlytics;

/**
 * Created by muniraju on 24/02/16.
 */
public class DelayAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 500;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
    private ProgressBar mLoadingIndicator;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DelayAutoCompleteTextView(Context context) {
        super(context);
    }

    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }
        super.onFilterComplete(count);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if(!focused) {
            if (mLoadingIndicator != null && mLoadingIndicator.getVisibility() == VISIBLE) {
                mLoadingIndicator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setText(CharSequence text, boolean filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            super.setText(text, filter);
        } else {
            if (filter) {
                setText(text);
            } else {
                ListAdapter adapter = getAdapter();
                if (adapter instanceof AddressAutoCompleteAdapter) {
                    setAdapter(null);
                    setText(text);
                    setAdapter((AddressAutoCompleteAdapter) adapter);
                } else {
                    setText(text);
                }

            }
        }
        if(text != null) {
            try {
                setSelection(text.length());
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
        }
    }

}
