package com.bigbasket.mobileapp.service;

/**
 * Created by jugal on 4/9/15.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.util.DataUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppDataUpdateService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int DELAY_IN_MINNUTES = 2;
    private Context context;

    @Override
    public void onCreate() {
        this.context = this;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(postInformation, 5, DELAY_IN_MINNUTES, TimeUnit.MINUTES);
    }

    private Runnable postInformation = new Runnable() {
        @Override
        public void run() {
            if (DataUtil.isInternetAvailable(context)) {
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.commit();
            }
        }
    };
}
