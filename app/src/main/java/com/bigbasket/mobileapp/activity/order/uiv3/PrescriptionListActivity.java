package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.PrescriptionListAdapter;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.SavedPrescription;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;


public class PrescriptionListActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        ArrayList<SavedPrescription> savedPrescriptionArrayList = marketPlace.getSavedPrescription();
        if (savedPrescriptionArrayList == null || savedPrescriptionArrayList.size() == 0)
            handler.sendEmptyMessage(ApiErrorCodes.INTERNAL_SERVER_ERROR, getString(R.string.server_error), true);
        renderPrescriptionList(savedPrescriptionArrayList);
    }

    private void renderPrescriptionList(final ArrayList<SavedPrescription> savedPrescriptionArrayList) {
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        if (contentView == null) return;
        contentView.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View prescriptionView = layoutInflater.inflate(R.layout.uiv3_fab_recycler_view, contentView, false);
        RecyclerView prescriptionRecyclerView = (RecyclerView) prescriptionView.findViewById(R.id.fabRecyclerView);
        UIUtil.configureRecyclerView(prescriptionRecyclerView, this, 1, 3);
        PrescriptionListAdapter prescriptionListAdapter = new PrescriptionListAdapter<>(getCurrentActivity(),
                savedPrescriptionArrayList, faceRobotoRegular);
        prescriptionRecyclerView.setAdapter(prescriptionListAdapter);

        FloatingActionButton floatingActionButton = (FloatingActionButton) prescriptionView.findViewById(R.id.btnFab);
        floatingActionButton.attachToRecyclerView(prescriptionRecyclerView);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPrescription();
            }
        });

        contentView.addView(prescriptionView);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }


    private void addNewPrescription() {
        Intent intent = new Intent(getCurrentActivity(), UploadNewPrescriptionActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == Constants.PRESCRIPTION_UPLOADED) {
            setResult(Constants.PRESCRIPTION_UPLOADED, data);
            getCurrentActivity().finish();// fix for back button press
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onCOReserveQuantityCheck() {
        Intent data = new Intent();
        data.putExtra(Constants.CO_RESERVE_QTY_DATA, getCOReserveQuantity());
        setResult(Constants.PRESCRIPTION_CHOSEN, data);
        getCurrentActivity().finish();// fix for back button press
    }

    @Override
    public String getScreenTag(){
        return TrackEventkeys.PRESCRIPTION_LISTING_SCREEN;
    }
}
