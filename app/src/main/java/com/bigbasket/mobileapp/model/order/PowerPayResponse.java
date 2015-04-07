package com.bigbasket.mobileapp.model.order;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.util.Constants;

public class PowerPayResponse {
    private String txnId;
    private String pgTxnId;
    private String dataPickupCode;
    private boolean isSuccess;
    private static PowerPayResponse powerPayResponse;

    private PowerPayResponse(String txnId, String pgTxnId, String dataPickupCode,
                             boolean isSuccess) {
        this.txnId = txnId;
        this.pgTxnId = pgTxnId;
        this.dataPickupCode = dataPickupCode;
        this.isSuccess = isSuccess;
    }

    public static void createInstance(Context ctx, String txnId, String pgTxnId,
                                      String dataPickupCode, boolean isSuccess) {
        powerPayResponse = new PowerPayResponse(txnId, pgTxnId, dataPickupCode, isSuccess);
        saveTxnDetailToPreference(ctx);
    }

    public static PowerPayResponse getInstance(Context ctx) {
        if (powerPayResponse == null) {
            setUpInstanceFromPreference(ctx);
        }
        return powerPayResponse;
    }

    private static void saveTxnDetailToPreference(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.TXN_ID, powerPayResponse.txnId);
        editor.putString(Constants.PG_TXN_ID, powerPayResponse.pgTxnId);
        editor.putString(Constants.DATA_PICKUP_CODE, powerPayResponse.dataPickupCode);
        editor.putBoolean(Constants.IS_PP_SUCCESS, powerPayResponse.isSuccess);
        editor.commit();
    }

    private static void setUpInstanceFromPreference(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String txnId = preferences.getString(Constants.TXN_ID, null);
        String pgTxnId = preferences.getString(Constants.PG_TXN_ID, null);
        String dataPickupCode = preferences.getString(Constants.DATA_PICKUP_CODE, null);
        boolean isSuccess = preferences.getBoolean(Constants.IS_PP_SUCCESS, false);
        if (txnId != null && pgTxnId != null && dataPickupCode != null) {
            createInstance(ctx, txnId, pgTxnId, dataPickupCode, isSuccess);
        }
    }

    public static void clearTxnDetail(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.TXN_ID);
        editor.remove(Constants.PG_TXN_ID);
        editor.remove(Constants.DATA_PICKUP_CODE);
        editor.commit();
        powerPayResponse = null;
    }

    public String getTxnId() {
        return txnId;
    }

    public String getPgTxnId() {
        return pgTxnId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getDataPickupCode() {
        return dataPickupCode;
    }
}
