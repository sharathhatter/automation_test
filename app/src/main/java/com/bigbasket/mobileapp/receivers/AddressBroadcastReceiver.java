package com.bigbasket.mobileapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.util.Constants;

import java.lang.ref.WeakReference;

public class AddressBroadcastReceiver<T extends BBActivity> extends BroadcastReceiver {

    private WeakReference<T> activityWeakRef;

    public AddressBroadcastReceiver(T activity) {
        this.activityWeakRef = new WeakReference<>(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (activityWeakRef != null && activityWeakRef.get() != null
                && !activityWeakRef.get().isSuspended()) {
            if (intent.getBooleanExtra(Constants.STATUS, false)) {
                activityWeakRef.get().onAddressSynced();
            } else {
                activityWeakRef.get().onAddressSyncFailure();
            }
        }
    }
}
