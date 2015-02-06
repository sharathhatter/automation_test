package com.bigbasket.mobileapp.adapter.order;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.model.order.PrescriptionImageModel;

import java.util.ArrayList;

/**
 * Created by jugal on 2/9/14.
 */
public class PrescriptionImageAdapter {

    private Context context;

    public PrescriptionImageAdapter(Context context) {
        this.context = context;
        open();
    }

    public static final String COLUMN_ID = "_Id";
    public static final String COLUMN_PRESCRIPTION_ID = "pharma_prescription_id";
    public static final String COLUMN_CHUNK_NUMBER = "chunk_number";
    public static final String COLUMN_MAX_CHUNKS = "max_chunks";
    public static final String COLUMN_PRESCRIPTION_IMAGE_CHUNK = "prescription_image_chunk";
    public static final String COLUMN_IMAGE_SEQUENCE = "image_sequence";
    public static final String tableName = "prescriptionImage";

    public static String createTable = String.format("CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%3$s TEXT NOT NULL, %4$s TEXT NOT NULL, %5$s TEXT, %6$s TEXT, %7$s TEXT NOT NULL );", tableName, COLUMN_ID,
            COLUMN_PRESCRIPTION_ID, COLUMN_CHUNK_NUMBER, COLUMN_MAX_CHUNKS, COLUMN_PRESCRIPTION_IMAGE_CHUNK, COLUMN_IMAGE_SEQUENCE
    );


    public void open() {
        DatabaseHelper.getInstance(context).open(context);
    }

    public void close() {
        DatabaseHelper.getInstance(context).close();
    }

    public void insert(String prescriptionId, String chunkNumber, String maxChunks,
                       String prescriptionImageChunk, String imageSequence) {//SQLException
        try {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PRESCRIPTION_ID, prescriptionId);
            cv.put(COLUMN_CHUNK_NUMBER, chunkNumber);
            cv.put(COLUMN_MAX_CHUNKS, maxChunks);
            cv.put(COLUMN_PRESCRIPTION_IMAGE_CHUNK, prescriptionImageChunk);
            cv.put(COLUMN_IMAGE_SEQUENCE, imageSequence);
            DatabaseHelper.db.insert(tableName, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getCursorForAllRows() {
        return DatabaseHelper.db.query(tableName, new String[]{COLUMN_PRESCRIPTION_ID, COLUMN_CHUNK_NUMBER, COLUMN_MAX_CHUNKS,
                COLUMN_PRESCRIPTION_IMAGE_CHUNK, COLUMN_IMAGE_SEQUENCE}
                , null, null, null, null, null);
    }

    public ArrayList<PrescriptionImageModel> getAllPrescriptionImages() {
        Cursor cursor = null;
        ArrayList<PrescriptionImageModel> prescriptionImageModelArrayList = null;
        try {
            cursor = getCursorForAllRows();
            if (cursor.moveToFirst()) {
                prescriptionImageModelArrayList = new ArrayList<>();
                do {
                    PrescriptionImageModel prescriptionImageModel = new PrescriptionImageModel(cursor);
                    prescriptionImageModelArrayList.add(prescriptionImageModel);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return prescriptionImageModelArrayList;
    }

    public void deleteUploadedRow(String chunkNumber) {
        try {
            DatabaseHelper.db.delete(tableName, COLUMN_CHUNK_NUMBER + "=" + chunkNumber, null);
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isTableExists() {
        Cursor cursor = DatabaseHelper.db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
                new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public boolean hasData() {
        Cursor cursor = getCursorForAllRows();
        if (cursor != null && cursor.getCount() > 0) return true;
        return false;
    }

    public boolean exists() {
        Cursor cursor = getCursorForAllRows();
        boolean val = cursor.moveToFirst();
        cursor.close();
        return val;
    }

}
