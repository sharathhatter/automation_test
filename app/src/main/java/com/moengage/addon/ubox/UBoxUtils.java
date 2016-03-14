/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moe.pushlibrary.providers.MoEDataContract;
import com.moe.pushlibrary.utils.MoEHelperConstants;
import com.moe.pushlibrary.utils.MoEHelperUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.moe.pushlibrary.MoEHelper.APP_DEBUG;
import static com.moe.pushlibrary.MoEHelper.TAG;

/**
 * A Utility class for Ubox addon
 *
 * @author MoEngage (abhishek@moengage.com)
 * @version 5.3.20
 * @since 2.5.0
 */
public final class UBoxUtils {

  private UBoxUtils() {
    // Intentionally made private
  }

  /**
   * Reads from a specified Uri and return a compressed byte array
   * with compression format {@link CompressFormat#JPEG}
   *
   * @param context An instance of the Application {@link Context}
   * @param uri     The {@link Uri} which represents the image
   * @return a compressed byte array of the image or null
   */
  @Nullable
  public static byte[] readBytes(@NonNull Context context, @NonNull Uri uri) {
    try {
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      Bitmap bm = MediaStore.Images.Media.getBitmap(
              context.getContentResolver(), uri);
      bm.compress(CompressFormat.JPEG, 60, byteBuffer);
      return byteBuffer.toByteArray();
    } catch (Exception e) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils:readBytes", e);
    }
    return null;
  }

  /**
   * Compresses an image to a maximum height of 816px x 612px.
   * This method also retains orientation and does a sampling to maintain 80% of the image quality
   *
   * @param context  An instance of the Application {@link Context}
   * @param imageUri The {@link Uri} which represents the image
   * @return a compressed byte array of the image or null
   */
  @Nullable
  public static byte[] compressImage(@NonNull Context context, @NonNull Uri imageUri) {
    Bitmap scaledBitmap = null;

    BitmapFactory.Options options = new BitmapFactory.Options();

    // by setting this field as true, the actual bitmap pixels are not
    // loaded in the memory. Just the bounds are loaded. If
    // you try the use the bitmap here, you will get null.
    options.inJustDecodeBounds = true;
    Bitmap bmp;
    try {
      bmp = BitmapFactory.decodeStream(context.getContentResolver()
              .openInputStream(imageUri), null, options);
    } catch (FileNotFoundException e1) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils: FileNotFound", e1);
      return readBytes(context, imageUri);
    }

    int actualHeight = options.outHeight;
    int actualWidth = options.outWidth;

    // max Height and width values of the compressed image is taken as
    // 816x612

    float maxHeight = 816.0f;
    float maxWidth = 612.0f;
    float imgRatio = actualWidth / actualHeight;
    float maxRatio = maxWidth / maxHeight;

    // width and height values are set maintaining the aspect ratio of the
    // image

    if (actualHeight > maxHeight || actualWidth > maxWidth) {
      if (imgRatio < maxRatio) {
        imgRatio = maxHeight / actualHeight;
        actualWidth = (int) (imgRatio * actualWidth);
        actualHeight = (int) maxHeight;
      } else if (imgRatio > maxRatio) {
        imgRatio = maxWidth / actualWidth;
        actualHeight = (int) (imgRatio * actualHeight);
        actualWidth = (int) maxWidth;
      } else {
        actualHeight = (int) maxHeight;
        actualWidth = (int) maxWidth;

      }
    }

    // setting inSampleSize value allows to load a scaled down version of
    // the original image

    options.inSampleSize = calculateInSampleSize(options, actualWidth,
            actualHeight);

    // inJustDecodeBounds set to false to load the actual bitmap
    options.inJustDecodeBounds = false;

    // this options allow android to claim the bitmap memory if it runs low
    // on memory
    options.inPurgeable = true;
    options.inInputShareable = true;
    options.inTempStorage = new byte[16 * 1024];

    try {
      // load the bitmap from its path
      bmp = BitmapFactory.decodeStream(context.getContentResolver()
              .openInputStream(imageUri), null, options);
    } catch (OutOfMemoryError exception) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils: OOM");
    } catch (FileNotFoundException e) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils: OOM", e);
      return readBytes(context, imageUri);
    }
    try {
      scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
              Bitmap.Config.ARGB_8888);
    } catch (OutOfMemoryError exception) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils: OOM");
    }

    if (null == scaledBitmap) return null;

    float ratioX = actualWidth / (float) options.outWidth;
    float ratioY = actualHeight / (float) options.outHeight;
    float middleX = actualWidth / 2.0f;
    float middleY = actualHeight / 2.0f;

    Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

    Canvas canvas = new Canvas(scaledBitmap);
    canvas.setMatrix(scaleMatrix);
    canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
            middleY - bmp.getHeight() / 2, new Paint(
                    Paint.FILTER_BITMAP_FLAG));

    // check the rotation of the image and display it properly
    ExifInterface exif;
    try {
      exif = new ExifInterface(imageUri.getPath());
      int orientation = exif.getAttributeInt(
              ExifInterface.TAG_ORIENTATION, 0);
      if (APP_DEBUG) Log.d(TAG, "Exif: " + orientation);
      Matrix matrix = new Matrix();
      if (orientation == 6) {
        matrix.postRotate(90);
      } else if (orientation == 3) {
        matrix.postRotate(180);
      } else if (orientation == 8) {
        matrix.postRotate(270);
      }
      scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
              scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
              true);
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      scaledBitmap.compress(CompressFormat.JPEG, 80, byteBuffer);
      return byteBuffer.toByteArray();
    } catch (Exception e) {
      if (APP_DEBUG)
        Log.e(TAG, "MoEAddOnChat-UBoxUtils: compressImage", e);
    }
    return null;
  }


  public static int calculateInSampleSize(BitmapFactory.Options options,
                                          int reqWidth, int reqHeight) {
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int heightRatio = Math.round((float) height
              / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }
    final float totalPixels = width * height;
    final float totalReqPixelsCap = reqWidth * reqHeight * 2;
    while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
      inSampleSize++;
    }

    return inSampleSize;
  }

  /**
   * Parses the stored notification payload to get the redirection intent
   * @param payload The message payload which needs to be parsed for redirection intent
   * @param context An instance of the application {@link Context}
   */
  @Nullable
  public static Intent getRedirectionIntent(String payload, Context context) {
    if (TextUtils.isEmpty(payload)) {
      return null;
    }
    try {
      JSONObject jsonMsg = new JSONObject(payload);

      String notificationType = jsonMsg.getString(ATTR_NOTIFY_TYPE);
      if (notificationType == null)
        return null;
      if (jsonMsg.has(MoEHelperConstants.GCM_EXTRA_COUPON_CODE)) {
        MoEHelperUtils.copyCouponCodeToClipboard(context, jsonMsg
                .getString(MoEHelperConstants.GCM_EXTRA_COUPON_CODE));
      }
      Intent redirectIntent = null;
      if (notificationType.equals(ATTR_NOTIFY_TYPE_WEB)) {
        Uri.Builder builder = Uri.parse(jsonMsg.getString(ATTR_WEB_URL)).buildUpon();
        builder.appendQueryParameter(MoEHelperConstants.NAVIGATION_PROVIDER_KEY, MoEHelperConstants.NAVIGATION_PROVIDER_VALUE);
        builder.appendQueryParameter(MoEHelperConstants.NAVIGATION_SOURCE_KEY, MoEHelperConstants.NAVIGATION_SOURCE_INBOX);
        redirectIntent = new Intent(Intent.ACTION_VIEW, builder.build());
      } else {
        String activityName = jsonMsg.getString(ATTR_REDIRECT_INTENT);
        try {
          redirectIntent = new Intent(context,
                  Class.forName(activityName));
          redirectIntent.putExtras(MoEHelperUtils
                  .convertJSONObjecttoBundle(jsonMsg));
          redirectIntent.setAction("" + System.currentTimeMillis());
          redirectIntent.putExtra(ATTR_KEY_INBOX, ATTR_VALUE_INBOX);
        } catch (Exception e) {
          Log.e(TAG, "UBoxUtils: Activity not found " + e.toString());
          return null;
        }
      }
      return redirectIntent;
    } catch (JSONException e1) {
      Log.e(TAG, "UBoxUtils: getRedirectionIntent " + e1.toString());
    }
    return null;
  }

  /**
   * Parses the chat payload to retrieve the linkified deep link
   * @param payload The message payload which needs to be parsed for redirection intent
   * @param context An instance of the application {@link Context}
   */
  @Nullable
  public static Intent getIntentForLinkifiedMessage(String payload, Context context) {
    try {
      Intent redirectIntent = null;
      JSONObject linkedObj = new JSONObject(payload);
      JSONObject andLink = linkedObj.getJSONObject(ATTR_OS_ANDROID);
      String url = andLink.optString(ATTR_DEEPLINK);
      if (TextUtils.isEmpty(url)) {
        try {
          String activityName = andLink.getString(ATTR_SCREENAME);
          redirectIntent = new Intent(context, Class.forName(activityName));
          Bundle extras = MoEHelperUtils.convertJSONObjecttoBundle(andLink.getJSONObject(ATTR_EXTRAS));
          redirectIntent.putExtras(extras);
          redirectIntent.setAction("" + System.currentTimeMillis());
          redirectIntent.putExtra(ATTR_KEY_INBOX, ATTR_VALUE_INBOX);
          return redirectIntent;
        } catch (Exception e) {
          Log.e(TAG, "UBoxUtils: getIntentForLinkifiedMessage " + e.toString());
          return null;
        }
      } else {
        Uri.Builder builder = Uri.parse(url.trim()).buildUpon();
        builder.appendQueryParameter(MoEHelperConstants.NAVIGATION_PROVIDER_KEY, MoEHelperConstants.NAVIGATION_PROVIDER_VALUE);
        builder.appendQueryParameter(MoEHelperConstants.NAVIGATION_SOURCE_KEY, MoEHelperConstants.NAVIGATION_SOURCE_INBOX);
        redirectIntent = new Intent(Intent.ACTION_VIEW, builder.build());
        return redirectIntent;
      }
    } catch (Exception e) {
      Log.e(TAG, "UBoxUtils: getIntentForLinkifiedMessage " + e.toString());
    }
    return null;
  }

  public static final String ATTR_LINKIFY = "linkify";
  public static final String ATTR_DEEPLINK = "DeepLinkUrl";
  public static final String ATTR_OS_ANDROID = "Android";
  public static final String ATTR_SCREENAME = "ScreenName";
  public static final String ATTR_EXTRAS = "KVPairs";
  public static final String ATTR_VALUE_INBOX = "inbox";
  public static final String ATTR_KEY_INBOX = "from";
  public static final String ATTR_REDIRECT_INTENT = "gcm_activityName";
  public static final String ATTR_WEB_URL = "gcm_webUrl";
  public static final String ATTR_NOTIFY_TYPE_WEB = "gcm_webNotification";
  public static final String ATTR_NOTIFY_TYPE = "gcm_notificationType";
  public static final String FILE_NAME_PREFIX = "JPEG_";
  public static final String FILE_NAME_EXTN = ".jpg";
  public static final String TIME_FORMAT = "yyyyMMdd_HHmmss";
  public static final String ATTR_GCM_MSG = "gcm_alert";
  public static final String ATTR_MSG_DETAILS = "text";
  public static final String ATTR_ID = "_id";

  /**
   * Checks if an inbox message has a coupon code
   *
   * @param payload the message payload
   * @return if the message has a coupon code
   */
  public static boolean hasCouponCode(String payload) {
    JSONObject obj = getJSONObject(payload);
    if (null == obj) throw new IllegalArgumentException("Did not pass the expected payload");
    return hasCouponCode(obj);
  }

  /**
   * Get the notification title from the payload
   *
   * @param payload The notification payload
   * @return The title of the notification
   */
  public static String getTitle(String payload) {
    JSONObject obj = getJSONObject(payload);
    if (null == obj) throw new IllegalArgumentException("Did not pass the expected payload");
    return getTitle(obj);
  }

  /**
   * Get the notification message from the payload
   *
   * @param payload The notification payload
   * @return The notification message
   */
  public static String getMessage(String payload) {
    JSONObject obj = getJSONObject(payload);
    if (null == obj) throw new IllegalArgumentException("Did not pass the expected payload");
    return getMessage(obj);
  }

  /**
   * Get the notification timestamp from the payload
   *
   * @param payload The notification payload
   * @return The notification timestamp
   */
  public static String getTimeStamp(String payload, String format) {
    JSONObject obj = getJSONObject(payload);
    if (null == obj) throw new IllegalArgumentException("Did not pass the expected payload");
    return getTimeStamp(obj, format);
  }

  /**
   * Get a coupon code associated with the notification
   *
   * @param payload The notification payload
   * @return The coupon code
   */
  public static String getCouponCode(String payload) {
    JSONObject obj = getJSONObject(payload);
    if (null == obj) throw new IllegalArgumentException("Did not pass the expected payload");
    return getCouponCode(obj);
  }

  @Nullable
  public static JSONObject getJSONObject(String payload) {
    try {
      return new JSONObject(payload);
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getJSONObject", e);
    }
    return null;
  }

  /**
   * Check if a notification payload has a coupon code or not
   *
   * @param payload The notification payload
   * @return true if the notification payload has coupon code
   */
  public static boolean hasCouponCode(@NonNull JSONObject payload) {
    return payload.has(MoEHelperConstants.GCM_EXTRA_COUPON_CODE);
  }

  /**
   * Get the notification title from the payload
   *
   * @param payload The notification payload
   * @return The title of the notification
   */
  public static String getTitle(JSONObject payload) {
    try {
      return payload.getString(MoEHelperConstants.GCM_EXTRA_TITLE);
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getTitle", e);
    }
    return null;
  }

  /**
   * Get the notification message from the payload
   *
   * @param payload The notification payload
   * @return The notification message
   */
  public static String getMessage(JSONObject payload) {
    try {
       return payload.getString(MoEHelperConstants.GCM_EXTRA_CONTENT);
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getMessage", e);
    }
    return null;
  }

  public static boolean isUserMessage(@NonNull Cursor cursor){
    String author = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_AUTHOR);
    return UnifiedInboxMessage.AUTHOR_USER.equals(author);
  }
  /**
   * Get the notification timestamp from the payload
   *
   * @param payload The notification payload
   * @return The notification timestamp
   */
  public static String getTimeStamp(JSONObject payload, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("US"));
    try {
      Date newDate = new Date(payload.getLong(MOE_MSG_RECEIVED_TIME));
      return sdf.format(newDate);
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getTimeStamp", e);
    }
    return null;
  }

  /**
   * Get a coupon code associated with the notification
   *
   * @param payload The notification payload
   * @return The coupon code
   */
  public static String getCouponCode(JSONObject payload) {
    if (hasCouponCode(payload)) {
      try {
        return payload.getString(MoEHelperConstants.GCM_EXTRA_COUPON_CODE);
      } catch (JSONException e) {
        if (APP_DEBUG) Log.e(TAG, "InboxUtils: getCouponCode", e);
      }
    }
    return null;
  }

  /**
   * Check if the notification
   *
   * @param cursor the cursor pointing
   *               to the {@link MoEDataContract.UnifiedInboxEntity#COLUMN_INDEX_MSG_CLICKED} column
   * @return true if the notification is clicked
   */
  public static boolean isClicked(Cursor cursor) {
    return cursor.getInt(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_CLICKED) == 1;
  }

  /**
   * @param payload the message payload
   * @return Action defined by user on click of the message.
   */
  @Nullable
  public static String getAction(String payload) {
    JSONObject jsonObject = getJSONObject(payload);
    if (jsonObject == null) throw new IllegalArgumentException("Did not pass the expected payload");
    String action = getString(jsonObject, MoEHelperConstants.GCM_EXTRA_ACTION);
    if (!TextUtils.isEmpty(action)) {
      return action;
    } else {
      return getString(jsonObject, MoEHelperConstants.GCM_EXTRA_ACTIVITY_NAME);
    }
  }

  /**
   * Retrieve a value for a specified key which was initially passed as an intent extra
   *
   * @param payload The notification payload
   * @param key The key you want to find in the payload
   *
   * @return the value of the key or null if it is not present
   */
  @Nullable
  public static String getString(JSONObject payload, String key) {
    try {
      return payload.getString(key);
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getAction", e);
    }
    return null;
  }

  /**
   * @param payload the message payload
   * @param key key for which the value is the be fetched.
   * @return value for a key,if key is present else null
   */
  @Nullable
  public static String getIntentExtras(String payload, String key) {
    JSONObject jsonObject = getJSONObject(payload);
    try {
      if (jsonObject != null) {
        if (jsonObject.has(key)) {
          return jsonObject.getString(key);
        }
      }
    } catch (JSONException e) {
      if (APP_DEBUG) Log.e(TAG, "InboxUtils: getValueFromKey", e);
    }
    return null;
  }

  public static final String MOE_MSG_RECEIVED_TIME = "MOE_MSG_RECEIVED_TIME";

  public static String getAlternateMessageDetails(String payload) throws JSONException{
    try{
      JSONObject obj = new JSONObject(payload);
      return obj.getString(UBoxUtils.ATTR_MSG_DETAILS);
    }catch(JSONException e){
      //ignored
    }
    return payload;
  }

  @Nullable
  public static String getAlternateMessageDetails(JSONObject obj){
    try{
      if(null == obj)return null;
      return obj.getString(UBoxUtils.ATTR_MSG_DETAILS);
    }catch(JSONException e){
      //ignored
    }
    return null;
  }

  public static boolean isLinkified(Cursor cursor){
    return TextUtils.isEmpty(cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_LINKIFY));
  }

}
