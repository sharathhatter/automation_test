package com.bigbasket.mobileapp.activity.account.uiv3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.bigbasket.mobileapp.util.Constants;
import com.moengage.addon.ubox.UnifiedInboxActivity;

import java.io.File;

/**
 * Created by manu on 4/12/15.
 */
public class BBUnifiedInboxActivity extends UnifiedInboxActivity {


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.moengage.addon.ubox.R.id.newImage) {
            handlePermission(Manifest.permission.CAMERA, Constants.PERMISSION_REQUEST_CODE_CAPTURE_CAMERA);
            return false;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void takePicture() {

        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = this.getOutputImageFile();
            if (photoFile != null) {
                takePictureIntent.putExtra("output", Uri.fromFile(photoFile));
                this.startActivityForResult(takePictureIntent, 10);
            }
        }
    }

    /**************
     * code for Android M Support
     ******************/

    public void handlePermission(String permission, int requestCode) {
        if (checkPermission(permission)) {
            this.takePicture();
        } else {
            requestPermission(permission, requestCode);
        }

    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAPTURE_CAMERA:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.CAMERA)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.takePicture();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
