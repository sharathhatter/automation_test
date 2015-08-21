package com.bigbasket.mobileapp.task.uiv3;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.CityManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsertPinCodeAsyncTask<T> extends AsyncTask<Object, Integer, Void> {

    private WeakReference<T> ctxRef;
    private HashMap<String, HashMap<String, ArrayList<String>>> pinCodeMaps;
    private HashMap<String, Integer> cityMap;

    public InsertPinCodeAsyncTask(T ctx, HashMap<String, HashMap<String, ArrayList<String>>> pinCodeMaps,
                                  HashMap<String, Integer> cityMap) {
        this.ctxRef = new WeakReference<>(ctx);
        this.pinCodeMaps = pinCodeMaps;
        this.cityMap = cityMap;
    }

    @Override
    protected void onPreExecute() {
        if (ctxRef.get() != null && !((CancelableAware) ctxRef.get()).isSuspended()) {
            ((ProgressIndicationAware) ctxRef.get()).showProgressDialog("Setting up area & pin-codes...",
                    true, true);
        }
        super.onPreExecute();
    }

    @Override
    protected final Void doInBackground(Object... params) {
        if (ctxRef.get() == null) return null;

        AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(((ActivityAware) ctxRef.get()).
                getCurrentActivity());
        areaPinInfoAdapter.deleteData();

        int totalElements = 0;
        for (Map.Entry<String, HashMap<String, ArrayList<String>>> entrySet : pinCodeMaps.entrySet()) {
            HashMap<String, ArrayList<String>> pinCodeAreaMap = entrySet.getValue();
            for (Map.Entry<String, ArrayList<String>> pincodeAreaEntrySet : pinCodeAreaMap.entrySet()) {
                totalElements += pincodeAreaEntrySet.getValue().size();
            }
        }
        ProgressDialog progressDialog = ((ProgressIndicationAware) ctxRef.get()).getProgressDialog();
        if (progressDialog != null) {
            progressDialog.setMax(totalElements);
        }
        DatabaseHelper.db.beginTransaction();
        int i = 0;
        for (Map.Entry<String, HashMap<String, ArrayList<String>>> entrySet : pinCodeMaps.entrySet()) {
            String cityName = entrySet.getKey();
            HashMap<String, ArrayList<String>> pinCodeAreaMap = entrySet.getValue();
            for (Map.Entry<String, ArrayList<String>> pincodeAreaEntrySet : pinCodeAreaMap.entrySet()) {
                String pinCode = pincodeAreaEntrySet.getKey();
                ArrayList<String> areas = pincodeAreaEntrySet.getValue();
                for (String areaName : areas) {
                    i++;
                    areaPinInfoAdapter.insert(areaName, pinCode, cityName, cityMap.get(cityName));
                    publishProgress(i);
                }
            }
        }
        DatabaseHelper.db.setTransactionSuccessful();
        DatabaseHelper.db.endTransaction();

        if (ctxRef.get() != null) {
            CityManager.setAreaPinInfoDate(((ActivityAware) ctxRef.get()).getCurrentActivity());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (ctxRef.get() == null) return;
        ProgressDialog progressDialog = ((ProgressIndicationAware) ctxRef.get()).getProgressDialog();
        if (progressDialog != null) {
            progressDialog.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (ctxRef.get() == null) return;
        if (((CancelableAware) ctxRef.get()).isSuspended()) return;
        try {
            ((ProgressIndicationAware) ctxRef.get()).hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        ((PinCodeAware) ctxRef.get()).onPinCodeFetchSuccess();
    }
}