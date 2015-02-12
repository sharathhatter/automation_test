package com.bigbasket.mobileapp.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.model.order.PrescriptionImageModel;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;

import java.util.ArrayList;
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
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    ExecutorService executorService;
    private Context context;

    @Override
    public void onCreate() {
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
                if (DataUtil.isInternetAvailable(context)) {
                    for (int i = 0; i < prescriptionImageList.size(); i++) {
                        final PrescriptionImageModel prescriptionImageModel = prescriptionImageList.get(i);
                        postPrescriptionImages(prescriptionImageModel);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void postPrescriptionImages(final PrescriptionImageModel prescriptionImageModel) {
        if (!DataUtil.isInternetAvailable(context)) return; // this is service call
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        bigBasketApiService.uploadPrescriptionImages(prescriptionImageModel.getPharmaPrescriptionId(),
                prescriptionImageModel.getChunkNumber(),
                prescriptionImageModel.getMaxChunks(),
                prescriptionImageModel.getPrescriptionImageChunk(),
                prescriptionImageModel.getImageSequence(),
                new Callback<BaseApiResponse>() {
                    @Override
                    public void success(BaseApiResponse apiResponse, Response response) {
                        if (apiResponse.status == 0 && apiResponse.message.equals(Constants.SUCCESS)) {
                            deleteChuckFromLocalStorage(prescriptionImageModel.getChunkNumber());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                });
    }

    private void deleteChuckFromLocalStorage(String chunkNumber) {
        PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(context);
        prescriptionImageAdapter.deleteUploadedRow(chunkNumber);
    }
}
