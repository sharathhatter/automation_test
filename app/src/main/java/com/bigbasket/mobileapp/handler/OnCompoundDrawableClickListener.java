package com.bigbasket.mobileapp.handler;

import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class OnCompoundDrawableClickListener implements View.OnTouchListener {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DRAWABLE_LEFT, DRAWABLE_RIGHT})
    public @interface DrawableType {
    }

    public static final int DRAWABLE_LEFT = 0;
    public static final int DRAWABLE_RIGHT = 2;

    private
    @DrawableType
    Integer[] drawableTypes;

    public OnCompoundDrawableClickListener(@DrawableType Integer... drawableTypes) {
        this.drawableTypes = drawableTypes;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event != null && event.getAction() == MotionEvent.ACTION_DOWN
                && v instanceof TextView) {
            TextView textView = (TextView) v;
            for (Integer drawableType : drawableTypes) {
                if (drawableType == null) continue;
                if (!TextUtils.isEmpty(textView.getError())
                        || textView.getCompoundDrawables()[drawableType] == null) return false;
                if (drawableType == DRAWABLE_RIGHT) {
                    int leftEdgeOfRightDrawable = textView.getRight() -
                            textView.getCompoundDrawables()[drawableType].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    leftEdgeOfRightDrawable -= textView.getPaddingRight();
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        onRightDrawableClicked();
                        return true;
                    }
                } else if (drawableType == DRAWABLE_LEFT) {
                    int rightEdgeOfLeftDrawable = textView.getLeft() +
                            textView.getCompoundDrawables()[drawableType].getBounds().width();
                    // when EditBox has padding, adjust rightEdge like
                    rightEdgeOfLeftDrawable += textView.getPaddingRight();
                    if (event.getRawX() <= rightEdgeOfLeftDrawable) {
                        onLeftDrawableClicked();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public abstract void onRightDrawableClicked();

    public abstract void onLeftDrawableClicked();
}
