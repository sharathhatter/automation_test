package com.bigbasket.mobileapp.model.order;

/**
 * Created by jugal on 28/8/14.
 */
public class ImageUploadModel {

    private String base64EncodedString;
    private boolean imageUploaded;

    public ImageUploadModel(String base64EncodedString, boolean imageUploaded) {
        this.base64EncodedString = base64EncodedString;
        this.imageUploaded = imageUploaded;
    }

    public String getBase64EncodedString() {
        return base64EncodedString;
    }

    public void setBase64EncodedString(String base64EncodedString) {
        this.base64EncodedString = base64EncodedString;
    }

    public boolean isImageUploaded() {
        return imageUploaded;
    }

    public void setImageUploaded(boolean imageUploaded) {
        this.imageUploaded = imageUploaded;
    }
}
