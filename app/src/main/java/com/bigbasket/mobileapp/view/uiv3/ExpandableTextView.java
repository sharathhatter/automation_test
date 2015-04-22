package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

public class ExpandableTextView extends TextView implements View.OnClickListener{

    private boolean mIsExpanded;
    private View mChildView;

    public ExpandableTextView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, 0, 0);
        try {
            mIsExpanded = a.getBoolean(R.styleable.ExpandableTextView_isExpanded, false);
        } finally {
            a.recycle();
        }
        setOnClickListener(this);
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.mIsExpanded = isExpanded;
        if (mChildView != null) {
            mChildView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
        if (isExpanded) {
            this.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, ContextCompat.getDrawable(getContext(), R.drawable.crispy_arrow_down), null);
        } else {
            this.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, ContextCompat.getDrawable(getContext(), R.drawable.crispy_arrow_right), null);
        }
        invalidate();
        requestLayout();
    }

    @Override
    public void setVisibility(int visibility) {
        if (mChildView != null) {
            if (visibility == View.VISIBLE) {
                if (isExpanded()) {
                    mChildView.setVisibility(View.VISIBLE);
                }
            } else {
                mChildView.setVisibility(visibility);
            }
        }
        super.setVisibility(visibility);
    }

    public void setChildView(View childView) {
        this.mChildView = childView;
    }

    @Override
    public void onClick(View v) {
        setExpanded(!isExpanded());
    }
}
