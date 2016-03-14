package com.bigbasket.mobileapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.ads.conversiontracking.InstallReceiver;
import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.localytics.android.ReferralReceiver;

/**
 * Created by muniraju on 04/03/16.
 */
public class InstallReferrerReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        new InstallReceiver().onReceive(context, intent);
        new ReferralReceiver().onReceive(context, intent);
        new CampaignTrackingReceiver().onReceive(context, intent);
        new com.moe.pushlibrary.InstallReceiver().onReceive(context, intent);

    }
}
