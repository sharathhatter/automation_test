package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private CheckedTextView mCheckedTextView;

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof CheckedTextView) {
                mCheckedTextView = (CheckedTextView) v;
            } else if (v instanceof ViewGroup) {
                int childChildCount = ((ViewGroup) v).getChildCount();
                for (int j = 0; j < childChildCount; j++) {
                    View v1 = ((ViewGroup) v).getChildAt(j);
                    if (v1 instanceof CheckedTextView) {
                        mCheckedTextView = (CheckedTextView) v1;
                        break;
                    }
                }
            }
            if (mCheckedTextView != null) {
                break;
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (mCheckedTextView != null) {
            mCheckedTextView.setChecked(checked);
        }
    }

    @Override
    public boolean isChecked() {
        return mCheckedTextView != null && mCheckedTextView.isChecked();
    }

    @Override
    public void toggle() {
        if (mCheckedTextView != null) {
            mCheckedTextView.toggle();
        }
    }
}