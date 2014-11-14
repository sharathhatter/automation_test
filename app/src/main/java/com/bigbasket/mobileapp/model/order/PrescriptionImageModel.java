package com.bigbasket.mobileapp.model.order;

import android.database.Cursor;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;

/**
 * Created by jugal on 3/9/14.
 */
public class PrescriptionImageModel {
    private String pharmaPrescriptionId;
    private String chunkNumber;
    private String maxChunks;
    private String prescriptionImageChunk;
    private String imageSequence;

    public PrescriptionImageModel(String pharmaPrescriptionId, String chunkNumber,
                                  String maxChunks, String prescriptionImageChunk,
                                  String imageSequence) {
        this.pharmaPrescriptionId = pharmaPrescriptionId;
        this.chunkNumber = chunkNumber;
        this.maxChunks = maxChunks;
        this.prescriptionImageChunk = prescriptionImageChunk;
        this.imageSequence = imageSequence;
    }

    public PrescriptionImageModel(Cursor cursor) {
        pharmaPrescriptionId = cursor.getString(cursor.getColumnIndex(PrescriptionImageAdapter.COLUMN_PRESCRIPTION_ID));
        chunkNumber = cursor.getString(cursor.getColumnIndex(PrescriptionImageAdapter.COLUMN_CHUNK_NUMBER));
        maxChunks = cursor.getString(cursor.getColumnIndex(PrescriptionImageAdapter.COLUMN_MAX_CHUNKS));
        prescriptionImageChunk = cursor.getString(cursor.getColumnIndex(PrescriptionImageAdapter.COLUMN_PRESCRIPTION_IMAGE_CHUNK));
        imageSequence = cursor.getString(cursor.getColumnIndex(PrescriptionImageAdapter.COLUMN_IMAGE_SEQUENCE));
    }

    public String getPharmaPrescriptionId() {
        return pharmaPrescriptionId;
    }

    public String getChunkNumber() {
        return chunkNumber;
    }

    public String getMaxChunks() {
        return maxChunks;
    }

    public String getPrescriptionImageChunk() {
        return prescriptionImageChunk;
    }

    public String getImageSequence() {
        return imageSequence;
    }
}
