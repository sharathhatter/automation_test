package com.bigbasket.mobileapp.handler.payment;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.holders.InMemMobikwikResponseHolder;

public final class MobikwikResponseHandler {

    @Nullable
    private static MobikwikResponseHandler mobikwikResponseHandler;

    private InMemMobikwikResponseHolder inMemMobikwikResponseHolder;

    private MobikwikResponseHandler(InMemMobikwikResponseHolder inMemMobikwikResponseHolder) {
        this.inMemMobikwikResponseHolder = inMemMobikwikResponseHolder;
    }

    public static void setMobikwikTransaction(Intent intent) {
        String txnId = intent.getStringExtra("orderid");
        String statusCode = intent.getStringExtra("statuscode");
        mobikwikResponseHandler = new MobikwikResponseHandler(new InMemMobikwikResponseHolder(txnId, statusCode));
    }

    public static void clear() {
        mobikwikResponseHandler = null;
    }

    @Nullable
    public static String getLastTransactionID() {
        return mobikwikResponseHandler != null
                && mobikwikResponseHandler.inMemMobikwikResponseHolder != null ?
                mobikwikResponseHandler.inMemMobikwikResponseHolder.getMobikwikTxnId() : null;
    }

    public static boolean wasTransactionSuccessful() {
        return mobikwikResponseHandler != null
                && mobikwikResponseHandler.inMemMobikwikResponseHolder != null
                && !TextUtils.isEmpty(mobikwikResponseHandler.inMemMobikwikResponseHolder.getMobikwikOrderStatus())
                && Integer.parseInt(mobikwikResponseHandler.inMemMobikwikResponseHolder.getMobikwikOrderStatus()) == 0;
    }
}
