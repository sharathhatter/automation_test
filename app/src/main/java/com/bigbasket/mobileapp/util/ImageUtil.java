package com.bigbasket.mobileapp.util;

import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class ImageUtil<T> {

    private T ctx;

    public ImageUtil(T ctx) {
        this.ctx = ctx;
    }

    public Bitmap getBitmap(String pathOfInputImage) {
        try {
            int inWidth = 0;
            int inHeight = 0;

            InputStream in = new FileInputStream(pathOfInputImage);

            // decode image size (decode metadata only, not the whole image)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;

            // save width and height
            inWidth = options.outWidth;
            inHeight = options.outHeight;

            int dstHeight = inWidth;
            int dstWidth = inHeight;
            int imageSizeInMB = (dstHeight * dstWidth) / (1024 * 1024);
            if (imageSizeInMB > 18) {
                ((BaseActivity) ctx).showAlertDialog(null, ((BaseActivity) ctx).getString(R.string.largeImageError), Constants.LARGE_SIZE_IMAGE);
                return null;
            }
            while (imageSizeInMB > 1) { // previously 1
                dstHeight -= 10;
                dstWidth -= 10;
                imageSizeInMB = (dstHeight * dstWidth) / (1024 * 1024);
            }

            // decode full image pre-resized
            in = new FileInputStream(pathOfInputImage);
            options = new BitmapFactory.Options();
            // calc rought re-size (this is no exact resize)

            // calculate dstWidth and dstHeight

            options.inSampleSize = Math.max(inWidth / dstWidth, inHeight / dstHeight);
            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

            //int imageSizeInMB = roughBitmap.getByteCount()/(1024*1024);

            // calc exact destination size
            Matrix m = new Matrix();
            RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
            RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
            m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
            float[] values = new float[9];
            m.getValues(values);

            // resize bitmap
            Bitmap resizedBitmap;
            try {
                resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]),
                        (int) (roughBitmap.getHeight() * values[4]), true);
            } catch (OutOfMemoryError e) {
                resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]),
                        (int) (roughBitmap.getHeight() * values[4]), true);
            }
            return resizedBitmap;
        } catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
        } catch (OutOfMemoryError e) {
            ((BaseActivity) ctx).showAlertDialog(null, ((BaseActivity) ctx).getString(R.string.largeImageError), Constants.LARGE_SIZE_IMAGE);
            return null;
        }

        return null;
    }

    public Bitmap getImageBitmap(String pathOfInputImage) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = new FileInputStream(pathOfInputImage);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }

            Bitmap bitmap = null;
            in = new FileInputStream(pathOfInputImage);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                bitmap = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x,
                        (int) y, true);
                bitmap.recycle();
                bitmap = scaledBitmap;

                System.gc();
            } else {
                bitmap = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("bitmap size - width: ", bitmap.getWidth() + ", height: " +
                    bitmap.getHeight());
            return bitmap;
        } catch (IOException e) {
            Log.e(e.getMessage(), e.toString());
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public void insertToDB(String prescriptionId, ArrayList<byte[]> arrayListByteArray) {
        for (int i = 0; i < arrayListByteArray.size(); i++) {
            byte[] imageBytes = arrayListByteArray.get(i);
            int chunkNumber = 0, startPointer = 0, offset = 500000;
            int maxChunks = (int) Math.ceil((float) imageBytes.length / (float) offset);
            int imageSequence = i + 1;
            insertBase64StingToDataBase(imageBytes, offset, imageSequence, chunkNumber,
                    startPointer, maxChunks == 0 ? 1 : maxChunks, prescriptionId);
        }
    }


    private void insertBase64StingToDataBase(byte[] imageByte, int offset, int imageSequence, int chunkNumber,
                                             int startPointer, int maxChunks, String prescriptionId) {

        byte[] outputBytes;
        if (imageByte.length - startPointer <= offset) {
            outputBytes = new byte[offset];
            System.arraycopy(imageByte, startPointer, outputBytes, 0, imageByte.length - startPointer);
            insertToDataBase(outputBytes, imageSequence, maxChunks, chunkNumber, prescriptionId);
            chunkNumber++;
            return;
        }

        outputBytes = new byte[offset];
        System.arraycopy(imageByte, startPointer, outputBytes, 0, offset);
        insertToDataBase(outputBytes, imageSequence, maxChunks, chunkNumber, prescriptionId);
        chunkNumber++;
        insertBase64StingToDataBase(imageByte, offset, imageSequence++, chunkNumber, startPointer + offset, maxChunks, prescriptionId);

    }

    private void insertToDataBase(byte[] outputBytes, int imageSequence, int maxChunkSize, int chunkNumber,
                                  String prescriptionId) {
        PrescriptionImageAdapter prescriptionImageAdapter = null;
        try {
            String base64EncodedChunkedImage = Base64.encodeToString(outputBytes, Base64.DEFAULT);
            prescriptionImageAdapter = new PrescriptionImageAdapter(((BaseActivity) ctx));
            prescriptionImageAdapter.insert(prescriptionId, String.valueOf(chunkNumber), String.valueOf(maxChunkSize),
                    base64EncodedChunkedImage, String.valueOf(imageSequence));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
