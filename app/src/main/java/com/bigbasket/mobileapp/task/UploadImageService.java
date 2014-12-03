package com.bigbasket.mobileapp.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.model.order.PrescriptionId;
import com.bigbasket.mobileapp.model.order.PrescriptionImageModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.ImageUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UploadImageService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    ExecutorService executorService;
    private Context context;

    @Override
    public void onCreate() {
        Log.d("**********************************************************************", "Service Started");
        this.context = this;
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(postInformation);
    }

    private Runnable postInformation = new Runnable() {
        @Override
        public void run() {
            uploadImages();
        }
    };


    private void uploadImages() {
        try {
            PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(context);
            ArrayList<PrescriptionImageModel> prescriptionImageList = prescriptionImageAdapter.getAllPrescriptionImages();
            if (prescriptionImageList != null && prescriptionImageList.size() > 0) {
                Log.d("******************************************** Upload for image count=>", String.valueOf(prescriptionImageList.size()));
                if (DataUtil.isInternetAvailable(context)) {
                    for (int i = 0; i < prescriptionImageList.size(); i++) {
                        final PrescriptionImageModel prescriptionImageModel = prescriptionImageList.get(i);
                        postPrescriptionImages(prescriptionImageModel);

                        /*
                        HashMap<String, String> imagePayload = new HashMap<String, String>() {
                            {
                                put(Constants.PHARMA_PRESCRIPTION_ID, prescriptionImageModel.getPharmaPrescriptionId());
                            }

                            {
                                put(Constants.CHUNK_NUMBER, prescriptionImageModel.getChunkNumber());
                            }

                            {
                                put(Constants.MAX_CHUNKS, prescriptionImageModel.getMaxChunks());
                            }

                            {
                                put(Constants.PRESCRIPTION_IMAGE_CHUNK, prescriptionImageModel.getPrescriptionImageChunk());
                            }

                            {
                                put(Constants.IMAGE_SEQUENCE, prescriptionImageModel.getImageSequence());
                            }
                        };
                        Log.d("******************************************** Upload for image=>", String.valueOf(i));



                        AuthParameters authParameters = AuthParameters.getInstance(context);
                        HttpRequestData httpRequestData = new HttpRequestData(MobileApiUrl.getBaseAPIUrl() +
                                Constants.UPLOAD_PRESCRIPTION_IMAGE_CHUNK,
                                imagePayload, true,
                                authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                                authParameters.getOsVersion(), new BasicCookieStore(), new HashMap<Object, String>() {
                            {
                                put(Constants.CHUNK_NUMBER, prescriptionImageModel.getChunkNumber());
                            }
                        });

                        Log.d("******************************************** HttpRequestData", String.valueOf(httpRequestData.getAdditionalCtx()));
                        HttpOperationResult httpOperationResult = DataUtil.doHttpPost(httpRequestData);
                        String responseString = httpOperationResult.getReponseString();
                        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
                        int status = jsonObject.get(Constants.STATUS).getAsInt();
                        String msg = jsonObject.get(Constants.MESSAGE).getAsString();
                        if (status == 0 && msg.equals(Constants.SUCCESS)) {
                            HashMap<Object, String> hashMapChunk = httpOperationResult.getAdditionalCtx();
                            deleteChuckFromLocalStorage(hashMapChunk.get(Constants.CHUNK_NUMBER));
                        } else {
                            Log.e("***************************************Error response while uploading image", "");
                        }


                        */
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void postPrescriptionImages(final PrescriptionImageModel prescriptionImageModel){
        if(!DataUtil.isInternetAvailable(context)) {return;}
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        bigBasketApiService.uploadPrescriptionImages(prescriptionImageModel.getPharmaPrescriptionId(),
                prescriptionImageModel.getChunkNumber(),
                prescriptionImageModel.getMaxChunks(),
                prescriptionImageModel.getPrescriptionImageChunk(),
                prescriptionImageModel.getImageSequence(),
                new Callback<ApiResponse>() {
                    @Override
                    public void success(ApiResponse apiResponse, Response response) {
                        if (apiResponse.status == 0 && apiResponse.message.equals(Constants.SUCCESS)) {
                            deleteChuckFromLocalStorage(prescriptionImageModel.getChunkNumber());
                        } else {
                            Log.e("***************************************Error response while uploading image", "");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ((BaseActivity) context).showAlertDialog("server error");
                    }
                });
    }


    private void deleteChuckFromLocalStorage(String chunkNumber) {
        PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(context);
        prescriptionImageAdapter.deleteUploadedRow(chunkNumber);
    }
}
