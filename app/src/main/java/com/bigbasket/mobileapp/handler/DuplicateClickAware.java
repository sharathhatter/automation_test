package com.bigbasket.mobileapp.handler;

import android.os.SystemClock;
import android.view.View;

import com.bigbasket.mobileapp.util.MutableLong;

import java.lang.ref.WeakReference;

public abstract class DuplicateClickAware implements View.OnClickListener {

    private WeakReference<MutableLong> mutableLongWeakReference;

    public DuplicateClickAware(MutableLong mutableLong) {
        this.mutableLongWeakReference = new WeakReference<>(mutableLong);
    }

    @Override
    public void onClick(View view) {
        if (mutableLongWeakReference != null && mutableLongWeakReference.get() != null) {
            long elapsedTime = mutableLongWeakReference.get().val;
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - elapsedTime < 400) {
                return;
            }
            mutableLongWeakReference.get().val = currentTime;
        }
        onActualClick(view);
    }

    public abstract void onActualClick(View view);
}
