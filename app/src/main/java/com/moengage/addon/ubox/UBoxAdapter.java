/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is custom CursorAdapter which takes care of showing the data for the
 * unified inbox
 *
 * @author MoEngage (abhishek@moengage.com)
 * @version 5.0.1
 * @since 1.0.0
 */
public class UBoxAdapter extends CursorAdapter {

  LayoutInflater inflater;
  Drawable mAppIcon;
  UBoxManager.UboxAdapter uboxAdapter;
  UBoxFragment.UBoxAdapterCallbacks adapterCallbacks;
  Context mContext;

  public UBoxAdapter(Context context, Cursor c, UBoxFragment.UBoxAdapterCallbacks callbacks) {
    super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
    this.adapterCallbacks = callbacks;
    inflater = LayoutInflater.from(context);

    mAppIcon = ContextCompat.getDrawable(context, context.getApplicationInfo().icon);
    uboxAdapter = UBoxManager.getInstance().getUboxAdapter(context);
  }

  /*
   * (non-Javadoc)
   *
   * @see android.support.v4.widget.CursorAdapter#bindView(android.view.View,
   * android.content.Context, android.database.Cursor)
   */
  @Override
  public void bindView(View arg0, Context arg1, Cursor arg2) {
    uboxAdapter.bindDataInternal(arg2, uboxAdapter.getViewHolder(arg0));
    uboxAdapter.bindData(uboxAdapter.getViewHolder(arg0), arg1, arg2);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * android.support.v4.widget.CursorAdapter#newView(android.content.Context,
   * android.database.Cursor, android.view.ViewGroup)
   */
  @Override
  public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
    if (arg1 == null) return null;
    View newView = uboxAdapter.newView(arg0, arg1, arg2, inflater);
    newView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          UBoxManager.ItemHolder holder = (UBoxManager.ItemHolder) v.getTag();
          if (holder.retryRequired) {
            adapterCallbacks.retryFailedMessage(holder.id);
          } else {
            if (!holder.isClickable) {
              return;
            }
            if (!uboxAdapter.onItemClick(v, mContext)) {
              Intent redirectIntent = UBoxUtils.getIntentForLinkifiedMessage(holder.linkify, mContext.getApplicationContext());

              if (null != redirectIntent) {
                adapterCallbacks.startActivity(redirectIntent);
              }
            }
          }
        }catch (Exception e){
          Log.e("UBox", "onClick", e);
        }
      }
    });
    bindView(newView, arg0, arg1);
    return newView;
  }
}
