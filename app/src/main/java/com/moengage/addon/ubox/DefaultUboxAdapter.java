/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moe.pushlibrary.providers.MoEDataContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Umang Chamaria
 */
public class DefaultUboxAdapter extends UBoxManager.UboxAdapter<UBoxManager.ItemHolder> {

  private static final boolean DEBUG = MoEHelper.APP_DEBUG;
  Context mContext;
  Drawable mAppIcon;

  DefaultUboxAdapter(Context context) {
    mContext = context;
    mAppIcon = ContextCompat.getDrawable(context, context.getApplicationInfo().icon);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent,
                      LayoutInflater layoutInflater) {
    return layoutInflater.inflate(R.layout.item_ubox, parent, false);
  }

  @Override
  public void bindData(UBoxManager.ItemHolder itemHolder, Context context, Cursor cursor) {

    itemHolder.id = cursor.getLong(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_ID);
    itemHolder.date.setText(
            cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_TIMESTAMP));
    int status = cursor.getInt(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_STATUS);

    itemHolder.isClickable = false;
    itemHolder.clickIdentifier.setVisibility(View.GONE);

    itemHolder.type = cursor.getInt(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_TYPE);
    itemHolder.payload = cursor
            .getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_MSG_DETAILS);
    itemHolder.linkify = cursor.getString(MoEDataContract.UnifiedInboxEntity.COLUMN_INDEX_LINKIFY);

    JSONObject obj = null;
      try {
        obj = new JSONObject(itemHolder.payload);
        obj.put(UBoxUtils.ATTR_ID, cursor.getColumnIndex(MoEDataContract.UnifiedInboxEntity._ID));
        itemHolder.payload = obj.toString();
      } catch (JSONException e) {
        //ignored
      }
      itemHolder.imageCrm.setVisibility(View.GONE);
      itemHolder.imageUser.setVisibility(View.GONE);
      if (UBoxUtils.isUserMessage(cursor)) {
        itemHolder.imageUser.setVisibility(View.VISIBLE);

        itemHolder.contentHolder.setBackgroundResource(R.drawable.bubble_outgoing);

        itemHolder.pic.setImageBitmap(null);
        itemHolder.pic.setVisibility(View.GONE);

        itemHolder.message.setVisibility(View.VISIBLE);
        if( null == obj){
          itemHolder.message.setText(itemHolder.payload);
        }else{
          itemHolder.message.setText(UBoxUtils.getAlternateMessageDetails(obj));
        }

        if (UnifiedInboxMessage.STATUS_SENDING == status) {
          itemHolder.errorMsg.setText(R.string.lbl_sending_inprogress);
          itemHolder.retryRequired = false;
        } else if (UnifiedInboxMessage.STATUS_UPLOADING == status) {
          itemHolder.errorMsg.setText(R.string.lbl_sending_upload);
          itemHolder.retryRequired = true;
        } else if (UnifiedInboxMessage.STATUS_FAILED_TO_SEND == status) {
          itemHolder.errorMsg.setText(R.string.lbl_sending_failed);
          itemHolder.retryRequired = true;
        } else if (UnifiedInboxMessage.STATUS_UPLOADING_FAILED == status) {
          itemHolder.errorMsg.setText(R.string.lbl_upload_failed);
          itemHolder.retryRequired = true;
        } else if (UnifiedInboxMessage.STATUS_SENT == status) {
          itemHolder.errorMsg.setText(R.string.lbl_sent);
          itemHolder.retryRequired = false;
        }
      } else {
        itemHolder.message.setText(UBoxUtils.getMessage(itemHolder.payload));
        if (!TextUtils.isEmpty(itemHolder.linkify)) {
          itemHolder.isClickable = true;
          itemHolder.clickIdentifier.setVisibility(View.VISIBLE);
        }

        itemHolder.pic.setVisibility(View.GONE);
        itemHolder.errorMsg.setText("");

        itemHolder.imageCrm.setVisibility(View.VISIBLE);

        itemHolder.contentHolder.setBackgroundResource(R.drawable.bubble_incoming);

        if (itemHolder.type == UnifiedInboxMessage.MSG_TYPE_TEXT) {
          itemHolder.message.setVisibility(View.VISIBLE);
          //set message
        } else if (itemHolder.type == UnifiedInboxMessage.MSG_TYPE_HTML) {
          itemHolder.pic.setImageBitmap(null);
          itemHolder.pic.setVisibility(View.GONE);
          itemHolder.message.setVisibility(View.VISIBLE);
          try {
            itemHolder.message.setText(Html.fromHtml(UBoxUtils.getAlternateMessageDetails(itemHolder.payload)));
          } catch (JSONException e) {
            if (MoEHelper.APP_DEBUG) Log.e(MoEHelper.TAG, "UboxAdapter: bindData", e);
          }
        } else if (itemHolder.type == UnifiedInboxMessage.MSG_TYPE_PIC) {
          itemHolder.pic.setImageBitmap(null);
          itemHolder.pic.setVisibility(View.VISIBLE);
          Picasso.with(context).load(cursor.getString(MoEDataContract.UnifiedInboxEntity
                  .COLUMN_INDEX_SERVER_URL)).into(itemHolder.pic);
        }

      }
      itemHolder.contentHolder.forceLayout();

  }

  @Override
  public boolean onItemClick(View view, Context context) {
    return false;
  }

  @Override
  public UBoxManager.ItemHolder getViewHolder(View convertView) {
    UBoxManager.ItemHolder holder = (UBoxManager.ItemHolder) convertView.getTag();
    if (null == holder) {
      holder = new UBoxManager.ItemHolder();
      holder.completeView = convertView.findViewById(R.id.uboxView);
      holder.contentHolder = convertView.findViewById(R.id.contentHolder);
      //Common content for chat messages
      holder.message = (TextView) convertView.findViewById(R.id.txt_msg);
      holder.pic = (ImageView) convertView.findViewById(R.id.pic);
      holder.date = (TextView) convertView.findViewById(R.id.txt_timestamp);
      holder.errorMsg = (TextView) convertView.findViewById(R.id.txt_failed);

      holder.imageCrm = (ImageView) convertView.findViewById(R.id.picCrm);
      if (null != holder.imageCrm) {
        holder.imageCrm.setImageDrawable(mAppIcon);
      }
      holder.imageUser = (ImageView) convertView.findViewById(R.id.picUsr);

      holder.clickIdentifier = (ImageView) convertView.findViewById(R.id.clickIdentifier);
      holder.clickIdentifier.setOnClickListener(parentClickListener);
      holder.message.setOnClickListener(parentClickListener);
      holder.contentHolder.setOnClickListener(parentClickListener);

      convertView.setTag(holder);
    }
    return holder;
  }

  View.OnClickListener parentClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      View view = (View)v.getParent();
      view.performClick();
    }
  };
}
