package com.payu.payuui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by muniraju on 26/02/16.
 */
public interface BankFragmentCallback {
    void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter);
    void unregisterBroadcast(BroadcastReceiver broadcastReceiver);
    void onHelpUnavailable();
    void onBankError();
    void onHelpAvailable();
}
