package com.bigbasket.mobileapp.handler;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public abstract class OnRightCompoundDrawableClicked implements View.OnTouchListener {
    public static final int DRAWABLE_RIGHT = 2;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event != null && event.getAction() == MotionEvent.ACTION_DOWN
                && v instanceof EditText) {
            EditText editText = (EditText) v;
            if (!TextUtils.isEmpty(editText.getError())
                    || editText.getCompoundDrawables()[DRAWABLE_RIGHT] == null) return false;
            int leftEdgeOfRightDrawable = editText.getRight() -
                    editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
            // when EditBox has padding, adjust leftEdge like
            leftEdgeOfRightDrawable -= editText.getPaddingRight();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                onRightDrawableClicked();
                return true;
            }
        }
        return false;
    }

    public abstract void onRightDrawableClicked();
}
