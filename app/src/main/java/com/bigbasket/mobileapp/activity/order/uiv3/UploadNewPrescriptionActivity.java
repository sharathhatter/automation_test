package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.PrescriptionId;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.UploadImageService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.ImageUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.luminous.pick.Action;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UploadNewPrescriptionActivity extends BackButtonActivity {

    private EditText editTextPatientName, editTextDoctorName, editTextPrescriptionName;
    private ArrayList<byte[]> arrayListByteArray = new ArrayList<>();
    private String[] all_path;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        renderUploadNewPrescription();
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.prescription_upload_menu, menu);
        final MenuItem choosePrescription = menu.findItem(R.id.action_choose_prescription);
        choosePrescription.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                launchImageSelectActivity();
                return true;
            }
        });

        BBDrawerLayout drawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void renderUploadNewPrescription() {
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_upload_new_prescription_layout, contentView, false);
        editTextPrescriptionName = (EditText) base.findViewById(R.id.editTextPrescriptionName);
        editTextPatientName = (EditText) base.findViewById(R.id.editTextPatientName);
        editTextDoctorName = (EditText) base.findViewById(R.id.editTextDoctorName);
        contentView.addView(base);
        BaseActivity.showKeyboard(editTextPrescriptionName);
    }


    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    private void launchImageSelectActivity() {
        Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
        startActivityForResult(i, 200);
    }

    public void OnUploadButtonClicked(View view) {
        editTextPatientName.setError(null);
        editTextDoctorName.setError(null);
        editTextPrescriptionName.setError(null);
        validateFormFields();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }


    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            if (sourceName.equals(Constants.MORE_IMAGES)) {
                arrayListByteArray.clear();
                return;
            } else if (sourceName.equals(Constants.LARGE_SIZE_IMAGE)) {
                arrayListByteArray.clear();
                return;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    private void validateFormFields() {
        View focusView = null;
        if (isEditTextEmpty(editTextPatientName)) {
            reportFormInputFieldError(editTextPatientName, getString(R.string.error_field_required));
            focusView = editTextPatientName;
        }

        if (isEditTextEmpty(editTextDoctorName)) {
            reportFormInputFieldError(editTextDoctorName, getString(R.string.error_field_required));
            focusView = editTextDoctorName;
        }

        if (isEditTextEmpty(editTextPrescriptionName)) {
            reportFormInputFieldError(editTextPrescriptionName, getString(R.string.error_field_required));
            focusView = editTextPrescriptionName;
        }

        if (focusView != null) {
            focusView.requestFocus();
            return;
        } else if (arrayListByteArray == null || arrayListByteArray.size() == 0) {
            showAlertDialog(null, getString(R.string.imageMandatoryError));
            return;
        } else {
            serverCallForUploadPrescription();
        }
    }

    private void serverCallForUploadPrescription() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) handler.sendOfflineError();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getResources().getString(R.string.please_wait));
        bigBasketApiService.uploadPrescription(editTextPatientName.getText().toString(),
                editTextDoctorName.getText().toString(),
                editTextPrescriptionName.getText().toString(),
                new Callback<ApiResponse<PrescriptionId>>() {
                    @Override
                    public void success(ApiResponse<PrescriptionId> prescriptionIdApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (prescriptionIdApiResponse.status == 0) {
                            String prescriptionId = prescriptionIdApiResponse.apiResponseContent.pharmaPrescriptionId;
                            new ImageUtil<>(getCurrentActivity()).insertToDB(prescriptionId, arrayListByteArray);
                            startService(new Intent(getCurrentActivity(), UploadImageService.class));
                            onParmaPrescriptionUploaded(prescriptionId);
                            trackEvent(TrackingAware.PRE_CHECKOUT_PHARMA_PRESCRIPTION_CREATED, null);
                        } else {
                            handler.sendEmptyMessage(prescriptionIdApiResponse.status, prescriptionIdApiResponse.message);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private void onParmaPrescriptionUploaded(String prescriptionId) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putString(Constants.PHARMA_PRESCRIPTION_ID, prescriptionId);
        editor.commit();
        new COReserveQuantityCheckTask<>(getCurrentActivity(), prescriptionId).startTask();
    }

    @Override
    public void onCOReserveQuantityCheck() {
        Intent data = new Intent();
        data.putExtra(Constants.CO_RESERVE_QTY_DATA, getCOReserveQuantity());
        setResult(Constants.PRESCRIPTION_UPLOADED, data);
        getCurrentActivity().finish();// fix for back button press
    }

    private void showDialogMoreThen5Images() {
        showAlertDialog(null, getString(R.string.maxImageError), Constants.MORE_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");
            if (all_path != null && all_path.length > 5) {
                showDialogMoreThen5Images();
                return;
            }
            SamplePrescriptionImage samplePrescriptionImage = new SamplePrescriptionImage();
            samplePrescriptionImage.execute();
        }
    }

    private class SamplePrescriptionImage extends AsyncTask<String, Long, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isActivitySuspended) {
                cancel(true);
            } else {
                progressDialog = ProgressDialog.show(getCurrentActivity(), "", getString(R.string.please_wait), true, false);
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
            handler.sendEmptyMessage(NavigationCodes.PRESCRIPTION_IMAGE_SAMPLING, null);
            return null;
        }

    }

    public void doSampling() {
        boolean notBreak = true;
        for (int i = 0; i < all_path.length; i++) {
            byte[] imageByte = sampleImage(all_path[i]);
            if (imageByte == null) {
                arrayListByteArray.clear();
                notBreak = false;
                break;
            }
            arrayListByteArray.add(imageByte);
        }
        if (!notBreak)
            showToast(getString(R.string.errorWhileSampling));
    }

    private byte[] sampleImage(String filePath) {
        Bitmap bmpPic = new ImageUtil<>(getCurrentActivity()).getBitmap(filePath);
        int compressQuality = 100;
        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        if (bmpPic != null) {
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            return bmpStream.toByteArray();
        }
        return null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.UPLOAD_NEW_PRESCRIPTION_SCREEN;
    }

}
