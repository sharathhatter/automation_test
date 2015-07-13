package com.bigbasket.mobileapp.task.uiv3;

import android.os.AsyncTask;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsertPinCodeAsyncTask<T> extends AsyncTask<Object, Integer, Void> {

    private WeakReference<T> ctxRef;
    private HashMap<String, ArrayList<String>> pinCodeMaps;

    public InsertPinCodeAsyncTask(T ctx, HashMap<String, ArrayList<String>> pinCodeMaps) {
        this.ctxRef = new WeakReference<>(ctx);
        this.pinCodeMaps = pinCodeMaps;
    }

    @Override
    protected void onPreExecute() {
        if (ctxRef.get() != null && !((CancelableAware) ctxRef.get()).isSuspended()) {
            ((ProgressIndicationAware) ctxRef.get()).showProgressDialog("Please wait...");
        }
        super.onPreExecute();
    }

    @Override
    protected final Void doInBackground(Object... params) {
        if (ctxRef.get() == null) return null;
        AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(((ActivityAware) ctxRef.get()).
                getCurrentActivity());
        for (Map.Entry<String, ArrayList<String>> pinCodeMapEntry : pinCodeMaps.entrySet()) {
            for (String areaName : pinCodeMapEntry.getValue()) {
                areaPinInfoAdapter.insert(areaName.toLowerCase(), pinCodeMapEntry.getKey());
            }
        }
        return null;
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