package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.MultipleImagesPrescriptionAdapter;
import com.bigbasket.mobileapp.adapter.order.PrescriptionListAdapter;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.SavedPrescription;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melnykov.fab.FloatingActionButton;
import java.util.ArrayList;


public class PrescriptionListActivity extends BackButtonActivity {
    private ArrayList<SavedPrescription> savedPrescriptionArrayList;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        if (saveInstanceState != null) {
            savedPrescriptionArrayList = saveInstanceState.getParcelableArrayList(Constants.PRESCRIPTION_INTENT_DATA);
            if (savedPrescriptionArrayList != null) {
                renderPrescriptionList(savedPrescriptionArrayList);
                return;
            }
        }
        MarketPlace marketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        savedPrescriptionArrayList = marketPlace.getSavedPrescription();
        renderPrescriptionList(savedPrescriptionArrayList);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (savedPrescriptionArrayList != null) {
            outState.putParcelableArrayList(Constants.PRESCRIPTION_INTENT_DATA, savedPrescriptionArrayList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void renderPrescriptionList(final ArrayList<SavedPrescription> savedPrescriptionArrayList) {
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(base);
        contentView.addView(scrollView);


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View precriptionView = layoutInflater.inflate(R.layout.uiv3_fab_recycler_view, null);
        RecyclerView prescriptionRecyclerView = (RecyclerView) precriptionView.findViewById(R.id.fabRecyclerView);
        UIUtil.configureRecyclerView(prescriptionRecyclerView, this, 1, 3);
        PrescriptionListAdapter prescriptionListAdapter = new PrescriptionListAdapter(getCurrentActivity(),
                                                                            savedPrescriptionArrayList);
        prescriptionRecyclerView.setAdapter(prescriptionListAdapter);

        FloatingActionButton floatingActionButton = (FloatingActionButton) precriptionView.findViewById(R.id.btnFab);
        floatingActionButton.attachToRecyclerView(prescriptionRecyclerView);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPrescription();
            }
        });

        base.addView(precriptionView);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }


    private void addNewPrescription(){
        Intent intent = new Intent(getCurrentActivity(), UploadNewPrescriptionActivity.class);
        startActivityForResult(intent, Constants.GO_TO_HOME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.PRESCRIPTION_UPLOADED) {
            getCurrentActivity().setResult(Constants.PRESCRIPTION_UPLOADED);
            getCurrentActivity().finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String responseString = httpOperationResult.getReponseString();
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
        if (httpOperationResult.getUrl().contains(Constants.GET_PRSCRIPTION_IMAGES)) {
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            if (status == 0) {
                JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                JsonArray jsonArrayImageUrls = responseJsonObject.get("image_urls").getAsJsonArray();
                if(jsonArrayImageUrls.size()>0) {
                    showPrescriptionImageDialog(jsonArrayImageUrls);
                }else {
                    showAlertDialog(getCurrentActivity(), null, "Images are uploading....");
                }
            }else {
                String msgString = status == ExceptionUtil.INTERNAL_SERVER_ERROR ?
                        getResources().getString(R.string.INTERNAL_SERVER_ERROR) :
                        jsonObject.get(Constants.MESSAGE).getAsString();
                showAlertDialog(getCurrentActivity(), null, msgString, Constants.GO_TO_HOME_STRING);
            }
        }
    }


    private void showPrescriptionImageDialog(JsonArray jsonArrayImageUrls){
        final Dialog prescriptionImageDialog = new Dialog(getCurrentActivity());
        prescriptionImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        prescriptionImageDialog.setCanceledOnTouchOutside(true);
        ListView listView = new ListView(getCurrentActivity());
        listView.setDividerHeight(0);
        listView.setDivider(null);
        prescriptionImageDialog.setContentView(listView);

        Rect displayRectangle = new Rect();
        getCurrentActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        prescriptionImageDialog.getWindow().setLayout(displayRectangle.width() - 20,
                (int) (displayRectangle.height() * 0.7f));

        ArrayList<Object> arrayListImgUrls = new ArrayList<>();
        for(int i=0; i<jsonArrayImageUrls.size(); i++){
            arrayListImgUrls.add(jsonArrayImageUrls.get(i).getAsString());
        }

        MultipleImagesPrescriptionAdapter multipleImagesPrescriptionAdapter = new MultipleImagesPrescriptionAdapter(getCurrentActivity(),
                arrayListImgUrls);
        listView.setAdapter(multipleImagesPrescriptionAdapter);
        prescriptionImageDialog.show();

    }

}
