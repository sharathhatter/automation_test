/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moe.pushlibrary.providers.MoEDataContract;

import static com.moe.pushlibrary.MoEHelper.TAG;

/**
 * Manager class which has ability to customize and control the behavior of the Notification Center
 * also known as the Inbox.
 *
 * @author MoEngage (abhishek@moengage.com)
 * @version 1.0
 * @since 2.0.0
 */
public final class UBoxManager {

  //Constructor
  public UBoxManager() {
  }
  //Singleton implementation
  public static UBoxManager _INSTANCE;

  public static UBoxManager getInstance() {
    if (null == _INSTANCE) {
      _INSTANCE = new UBoxManager();
    }
    return _INSTANCE;
  }

  public UboxAdapter customizer;

  /**
   * Set the {@link UboxAdapter} which will be used MoE Inbox
   */
  public void setUboxAdapter(UboxAdapter customizer) {
    this.customizer = customizer;
  }

  /**
   * Get the {@link UboxAdapter} if any
   */
  public UboxAdapter getUboxAdapter(Context context) {
    if (null == customizer) {
      return new DefaultUboxAdapter(context);
    }
    return this.customizer;
  }

  /**
   * Base class for Inbox Customization
   */
  public static abstract class UboxAdapter<VH extends ViewHolder> {

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     * correct position.
     * @param parent The parent to which the new view is attached to
     * @return the new inflated view which will be used by the adapter
     */
    public abstract View newView(Context context, Cursor cursor, ViewGroup parent,
        LayoutInflater layoutInflater);

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param context Interface to application's global information
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     * correct position.
     * @return The binded View which is ready to be shown
     */
    public abstract void bindData(VH holder, Context context, Cursor cursor);

    public void bindDataInternal(Cursor cursor, VH holder){
      try{
        UnifiedInboxMessage message = new UnifiedInboxMessage();
        message._id = cursor.getLong(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_ID);
        message.gtime = cursor.getLong(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_GTIME);
        message.msgClicked = cursor.getInt(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_CLICKED);
        message.msgTtl = cursor.getLong(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_TTL);
        message.details = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_DETAILS);
        message.author = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_AUTHOR);
        message.blobId = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_BLOB_ID);
        message.linkify = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_LINKIFY);
        message.localUri = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_CONTENT_URI);
        message.serverUri = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_SERVER_URL);
        message.msg_id = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_ID);
        message.status = cursor.getInt(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_STATUS);
        message.timestamp = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_TIMESTAMP);
        holder.message = message;
      } catch (Exception e) {
      Log.e(TAG, "bindData", e);
      }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     *
     * @param view The view within the AdapterView that was clicked (this will be a view provided
     * by the adapter)
     * @param context Application Context
     * @return true if item click is overridden // currently not honored
     */
    public abstract boolean onItemClick(View view,
        Context context);

    /**
     * Return the ViewHolder from this method which will be used to reduce Hierarchy lookup and also
     * be used to piggy back data required when view is clicked
     *
     * @param convertView The view which is used by the adapter
     * @return The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     */
    public abstract VH getViewHolder(View convertView);
  }

  public static class ViewHolder {
    public UnifiedInboxMessage message;
  }

  public static class ItemHolder extends ViewHolder {
    public long id;
    public ImageView clickIdentifier;
    public int type;
    public TextView message;
    public TextView date;
    public TextView errorMsg;
    public View contentHolder;
    public ImageView imageUser;
    public ImageView imageCrm;
    public ImageView pic;
    public boolean isClickable;
    public String payload;
    public String linkify;
    public boolean retryRequired;
    public View completeView;
  }
}
