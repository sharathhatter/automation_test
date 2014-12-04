package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.MultipleImagesPrescriptionAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.model.account.UpdatePin;
import com.bigbasket.mobileapp.model.order.PrescriptionId;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.UploadImageService;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;
import com.luminous.pick.Action;

import java.io.*;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UploadNewPrescriptionActivity extends BackButtonActivity {
    private static final String TAG = UploadNewPrescriptionActivity.class.getName();
    private ArrayList<Object> uploadImageList = new ArrayList<>();
    private EditText editTextPatientName, editTextDoctorName, editTextPrescriptionName;
    private TextView txtViewImages;
    private View base;
    private ArrayList<byte[]> arrayListByteArray = new ArrayList<>();
    private String[] all_path;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        renderUploadNewPrescription();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void renderUploadNewPrescription() {
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);
        contentView.addView(scrollView);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = inflater.inflate(R.layout.uiv3_upload_new_prescription_layout, null);
        txtViewImages = (TextView) base.findViewById(R.id.txtViewImages);
        txtViewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrescriptionImageDialog(uploadImageList);
            }
        });
        linearLayout.addView(base);
    }


    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    public void onSelectImagesFromGallery(View view) {
        uploadImageList.clear();
        txtViewImages.setVisibility(View.GONE);
        launchImageSelectActivity();
    }


    private void launchImageSelectActivity() {
        // For multiple images
        Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
        startActivityForResult(i, 200);
    }

    public void OnUploadButtonClicked(View view) {
        goBacktoPrescriptionList("1234");
        //validateFormFields();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }

    private void validateFormFields() {
        editTextPatientName = (EditText) base.findViewById(R.id.editTextPatientName);
        editTextDoctorName = (EditText) base.findViewById(R.id.editTextDoctorName);
        editTextPrescriptionName = (EditText) base.findViewById(R.id.editTextPrescriptionName);

        ArrayList<String> missingFields = new ArrayList<>();
        if (isEditTextEmpty(editTextPatientName)) {
            missingFields.add(editTextPatientName.getHint().toString());
            editTextPatientName.setError(null);
        }

        if (isEditTextEmpty(editTextDoctorName)) {
            missingFields.add(editTextDoctorName.getHint().toString());
            editTextDoctorName.setError(null);
        }

        if (isEditTextEmpty(editTextPrescriptionName)) {
            missingFields.add(editTextPrescriptionName.getHint().toString());
            editTextPrescriptionName.setError(null);
        }

        if (arrayListByteArray == null || arrayListByteArray.size() == 0) {
            missingFields.add("Prescription Image");
        }

        int missingFieldsListSize = missingFields.size();
        if (missingFieldsListSize > 0) {
            String msg;
            if (missingFieldsListSize == 1) {
                msg = missingFields.get(0) + " is mandatory";
            } else {
                msg = "Following fields are mandatory: " + UIUtil.sentenceJoin(missingFields);
            }
            showAlertDialog(this, null, msg);
            return;
        }

        //sendPharmaUploadRequestToServer();

        serverCallForUploadPrescription();

    }

    /*

    private void sendPharmaUploadRequestToServer() {

            HashMap<String, String> payload = new HashMap<String, String>() {
                {
                    put(Constants.PATIENT_NAME, editTextPatientName.getText().toString());
                }

                {
                    put(Constants.DOCTOR_NAME, editTextDoctorName.getText().toString());
                }

                {
                    put(Constants.PRESCRIPTION_NAME, editTextPrescriptionName.getText().toString());
                }
            };

            startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.UPLOAD_PRESCRIPTION, payload, true,
                    AuthParameters.getInstance(this), new BasicCookieStore());

    }
    */

    private void serverCallForUploadPrescription(){
        if(!DataUtil.isInternetAvailable(getCurrentActivity())) {return;}
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog("Please wait...");
        bigBasketApiService.uploadPrescription(editTextPatientName.getText().toString(),
                                                editTextDoctorName.getText().toString(),
                                                editTextPrescriptionName.getText().toString(),
                                                new Callback<ApiResponse<PrescriptionId>>() {
            @Override
            public void success(ApiResponse<PrescriptionId> prescriptionIdApiResponse, Response response) {
                hideProgressView();
                if(prescriptionIdApiResponse.status==0 && prescriptionIdApiResponse.message.equals(Constants.SUCCESS)){
                    String prescriptionId = prescriptionIdApiResponse.apiResponseContent.pharmaPrescriptionId;
                    ImageUtil.insertToDB(prescriptionId, arrayListByteArray);
                    startService(new Intent(getCurrentActivity(), UploadImageService.class));
                    goBacktoPrescriptionList(prescriptionId);
                }else {
                    String errorMsg = prescriptionIdApiResponse.status == ExceptionUtil.INTERNAL_SERVER_ERROR ?
                            getResources().getString(R.string.imageUploadFailed):prescriptionIdApiResponse.message;
                    showAlertDialog(getCurrentActivity(), null, errorMsg);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                showAlertDialog("server error");
            }
        });
    }

    /*
    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        String responseString = httpOperationResult.getReponseString();
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
        int status = jsonObject.get(Constants.STATUS).getAsInt();
        String msg = jsonObject.get(Constants.MESSAGE).getAsString();
        if (status == 0 && msg.equals(Constants.SUCCESS)) {
            if(httpOperationResult.getUrl().contains(Constants.UPLOAD_PRESCRIPTION)){
                JsonObject jsonResponseObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                String prescriptionId = jsonResponseObject.get(Constants.PHARMA_PRESCRIPTION_ID).getAsString();
                ImageUtil.insertToDB(prescriptionId, arrayListByteArray);
                startService(new Intent(this, UploadImageService.class));
                renderPrescriptionListActivity(prescriptionId);
            }
        } else {
            String errorMsg = status == ExceptionUtil.INTERNAL_SERVER_ERROR ? getResources().getString(R.string.imageUploadFailed):msg;
            showAlertDialogFinish(this, null, errorMsg);
        }
    }

    */


    private void goBacktoPrescriptionList(String prescriptionId){
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putString(Constants.PHARMA_PRESCRIPTION_ID,prescriptionId);
        editor.commit();
        new COReserveQuantityCheckTask<>(getCurrentActivity(), prescriptionId).execute();

    }

    @Override
    public void onCOReserveQuantityCheck() {
        setResult(Constants.PRESCRIPTION_UPLOADED);
        this.finish();
        Intent intent = new Intent(getCurrentActivity(), CheckoutQCActivity.class);
        intent.putExtra(Constants.QC_LEN, coReserveQuantity.getQc_len());
        startActivityForResult(intent, Constants.GO_TO_HOME);
    }

    private void showDialogMoreThen5Images(){
        showAlertDialog(getCurrentActivity(), null, "You can't upload more than 5 images", Constants.MORE_IMAGES);
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName) {
        super.onPositiveButtonClicked(dialogInterface, id, sourceName, null);
        if (sourceName != null) {
            if(sourceName.equals(Constants.MORE_IMAGES)){
                uploadImageList.clear();
                txtViewImages.setVisibility(View.GONE);
                return;
            }else if(sourceName.equals(Constants.LARGE_SIZE_IMAGE)){
                uploadImageList.clear();
                txtViewImages.setVisibility(View.GONE);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");
            if(all_path != null && all_path.length>5){
                showDialogMoreThen5Images();
                return;
            }
            SamplePrescriptionImage samplePrescriptionImage = new SamplePrescriptionImage();
            samplePrescriptionImage.execute();
        }
    }

    private class SamplePrescriptionImage extends AsyncTask<String, Long, Void> {

        private  ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isActivitySuspended) {
                cancel(true);
            } else {
                progressDialog = ProgressDialog.show(getCurrentActivity(), "", "Please wait", true, false);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            } else {
                return;
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }
            hRefresh.sendEmptyMessage(1);
            return null;
        }

    }

    Handler hRefresh = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    doSampling();
                    break;
            }
        }
    };

    private void doSampling(){
        boolean notBreak= true;
        for(int i=0; i<all_path.length; i++){
            byte[] imageByte= sampleImage(all_path[i]);
            if(imageByte == null) {
                uploadImageList.clear();
                arrayListByteArray.clear();
                notBreak = false;
                break;
            }
            arrayListByteArray.add(imageByte);
        }
        if(all_path.length>1)
            txtViewImages.setText(getResources().getString(R.string.viewPrescriptionImages));
        if(notBreak)
            txtViewImages.setVisibility(View.VISIBLE);
    }

    private byte[] sampleImage(String filePath){
        Bitmap bmpPic = ImageUtil.getBitmap(filePath, getCurrentActivity());
        int compressQuality = 100;
        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        if (bmpPic != null) {
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            int streamLength = bmpPicByteArray.length;
            Log.d(TAG, "Size: " + streamLength);
            uploadImageList.add(bmpPic);
            return bmpPicByteArray;
        }
        return null;
    }

    @Override
    public void onHttpError() {
        showAlertDialog(this, null, "A network error occurred, while uploading your prescription");
    }

//    public void onTxtViewImages(View view){
//        showPrescriptionImageDialog(uploadImageList);
//    }

    private void showPrescriptionImageDialog(ArrayList<Object> uploadImageList){
        final Dialog prescriptionImageDialog = new Dialog(this);
        prescriptionImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        prescriptionImageDialog.setCanceledOnTouchOutside(true);
        ListView listView = new ListView(this);
        listView.setDividerHeight(0);
        listView.setDivider(null);
        prescriptionImageDialog.setContentView(listView);

        Rect displayRectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        prescriptionImageDialog.getWindow().setLayout(displayRectangle.width() - 20,
                (int) (displayRectangle.height() * 0.7f));

        MultipleImagesPrescriptionAdapter multipleImagesPrescriptionAdapter = new MultipleImagesPrescriptionAdapter(this,
                uploadImageList);
        listView.setAdapter(multipleImagesPrescriptionAdapter);
        prescriptionImageDialog.show();

    }
}
