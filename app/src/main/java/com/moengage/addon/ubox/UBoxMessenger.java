/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.internal.APIManager;
import com.moe.pushlibrary.internal.MoEAsyncTask;
import com.moe.pushlibrary.internal.MoEAsyncTask.AsyncCallbacks;
import com.moe.pushlibrary.internal.MoEDAO;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moe.pushlibrary.providers.MoEDataContract.UnifiedInboxEntity;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * The Service which helps run the seamless chat service
 * 
 * @author MoEngage (abhishek@moengage.com)
 * @version 5.0.1
 * @since 5.0.1
 *
 */
public class UBoxMessenger extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( !mSynced ){
			new MoEAsyncTask<Void, Void>(callbackChatSync, null, getApplicationContext()).executeTask();
		}
		return START_STICKY;
	}

	public int mTaskCounter = 0;
	public boolean needToStopSelf = false;
	public boolean mSynced = false;
	public static final int MSG_REGISTER_CLIENT = 1;

	public static final int MSG_UNREGISTER_CLIENT = 2;

	public static final int MSG_SEND_CHAT = 3;
	
	public static final int MSG_UPLOAD_IMAGE = 4;
	
	public static final int MSG_KILL = 5;
	
	public static final int MSG_RETRY = 6;

	public static class IncomingHandler extends Handler {
		public final WeakReference<UBoxMessenger> mService;

		public IncomingHandler(UBoxMessenger service) {
			mService = new WeakReference<UBoxMessenger>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			UBoxMessenger currService = mService.get();
			if (null != currService) {
				currService.handleIncomingMessages(msg);
			}
		}
	}

	public void handleIncomingMessages(Message msg) {
		switch (msg.what) {
		case MSG_REGISTER_CLIENT:
			mTaskCounter++;
			mHandler.removeCallbacks(killRunnable);
			break;
		case MSG_UNREGISTER_CLIENT:
			mTaskCounter--;
			break;
		case MSG_SEND_CHAT:
			new MoEAsyncTask<UnifiedInboxMessage, Void>(callbackSendChatMessage, (UnifiedInboxMessage)msg.obj, getApplicationContext()).executeTask();
			break;
		case MSG_UPLOAD_IMAGE:
			new MoEAsyncTask<UnifiedInboxMessage, Void>(callbackUploadImage, (UnifiedInboxMessage)msg.obj, getApplicationContext()).executeTask();
			break;
		case MSG_RETRY:
			new MoEAsyncTask<Long, Void>(callbackRetrySend, (Long) msg.obj, getApplicationContext()).executeTask();
			break;
		case MSG_KILL:
			stopSelf();
			break;
		}
		if( mTaskCounter == 0){
			needToStopSelf = true;
		}else{
			needToStopSelf = false;
		}
	}

	public IncomingHandler mHandler = new IncomingHandler(this);
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(mHandler);

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	AsyncCallbacks<UnifiedInboxMessage, Void> callbackUploadImage = new AsyncCallbacks<UnifiedInboxMessage, Void>() {

		@Override
		public Void doInBackground(Context context, UnifiedInboxMessage input) {
			
			if(input.status == UnifiedInboxMessage.STATUS_FAILED_TO_SEND){
				input.status = UnifiedInboxMessage.STATUS_SENDING;
				updateSentStatus(context, UnifiedInboxMessage.STATUS_SENDING, input._id);
			} else if( input.status == UnifiedInboxMessage.STATUS_UPLOADING_FAILED ){
				input.status = UnifiedInboxMessage.STATUS_UPLOADING;
				updateSentStatus(context, UnifiedInboxMessage.STATUS_SENDING, input._id);
			} else {
				input.status = UnifiedInboxMessage.STATUS_UPLOADING;
				input._id = MoEDAO.getInstance(context).addChatMessage(context, input);
			}
			
			UnifiedInboxMessage res = APIManager.uploadImage(getApplicationContext(), UBoxUtils.compressImage(getApplicationContext(), Uri.parse(input.localUri)), input);
			if( null != res){
				Uri updateRecord = UnifiedInboxEntity.getContentUri(context).buildUpon().appendPath(String.valueOf(input._id)).build();
				ContentValues values = new ContentValues();
				values.put(UnifiedInboxEntity.BLOB_ID, res.blobId);
				values.put(UnifiedInboxEntity.SERVER_URL, res.serverUri);
				values.put(UnifiedInboxEntity.STATUS, UnifiedInboxMessage.STATUS_SENDING);
				int rows = context.getContentResolver().update(updateRecord, values, null, null);
				File filenew = new File(res.localUri);
				if ( null != filenew && filenew.length() == 0) {
					filenew.delete();
				}
				if( rows > 0){
					context.getContentResolver().notifyChange(updateRecord, null);
				}
				boolean result = APIManager.sendChatMessage(context, input);
				if(result){
					updateSentStatus(context, UnifiedInboxMessage.STATUS_SENT, input._id);
					
				}else{
					updateSentStatus(context, UnifiedInboxMessage.STATUS_FAILED_TO_SEND, input._id);
				}
			}else{
				updateSentStatus(context, UnifiedInboxMessage.STATUS_UPLOADING_FAILED, input._id);
			}
			return null;
		}

		@Override
		public void onTaskCompleted(Context context, Void result) {
			validateAndKillSelf();
		}
	};

	AsyncCallbacks<UnifiedInboxMessage, Void> callbackSendChatMessage = new AsyncCallbacks<UnifiedInboxMessage, Void>() {

		@Override
		public Void doInBackground(Context context, UnifiedInboxMessage input) {
			if( null == input)return null;
			if(input.status == UnifiedInboxMessage.STATUS_FAILED_TO_SEND){
				input.status = UnifiedInboxMessage.STATUS_SENDING;
				updateSentStatus(context, UnifiedInboxMessage.STATUS_SENDING, input._id);
			} else if( input.status == UnifiedInboxMessage.STATUS_UPLOADING_FAILED ){
				return callbackUploadImage.doInBackground(context, input);
			}else{
				input.status = UnifiedInboxMessage.STATUS_SENDING;
				input._id = MoEDAO.getInstance(context).addChatMessage(context, input);
			}
			boolean res = APIManager.sendChatMessage(context, input);
			if(res){
				updateSentStatus(context, UnifiedInboxMessage.STATUS_SENT, input._id);
			}else{
				updateSentStatus(context, UnifiedInboxMessage.STATUS_FAILED_TO_SEND, input._id);
			}
			return null;
		}

		@Override
		public void onTaskCompleted(Context context, Void result) {
			validateAndKillSelf();
		}
	};
	
	
	AsyncCallbacks<Long, Void> callbackRetrySend = new AsyncCallbacks<Long, Void>() {

		@Override
		public Void doInBackground(Context context, Long input) {
			Cursor cursor = context.getContentResolver().query(UnifiedInboxEntity.getContentUri(context).buildUpon().appendPath(String.valueOf(input)).build(), UnifiedInboxEntity.PROJECTION, null, null, null);
			if( null != cursor && cursor.moveToFirst()){
				UnifiedInboxMessage entity = new UnifiedInboxMessage();
				entity.status = cursor.getInt(UnifiedInboxEntity.COLUMN_INDEX_MSG_STATUS);
				entity._id = cursor.getLong(UnifiedInboxEntity.COLUMN_INDEX_ID);
				entity.author = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_AUTHOR);
				entity.blobId = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_BLOB_ID);
				entity.details = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_MSG_DETAILS);
				entity.gtime = cursor.getLong(UnifiedInboxEntity.COLUMN_INDEX_GTIME);
				entity.localUri = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_CONTENT_URI);
				entity.serverUri = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_SERVER_URL);
				entity.messageType = cursor.getInt(UnifiedInboxEntity.COLUMN_INDEX_MSG_TYPE);
				entity.msg_id = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_MSG_ID);
				entity.msgTtl = cursor.getLong(UnifiedInboxEntity.COLUMN_INDEX_MSG_TTL);
				entity.timestamp = cursor.getString(UnifiedInboxEntity.COLUMN_INDEX_TIMESTAMP);
				cursor.close();
				if ( entity.status == UnifiedInboxMessage.STATUS_UPLOADING_FAILED ){
					callbackUploadImage.doInBackground(context, entity);
				}else if( entity.status == UnifiedInboxMessage.STATUS_FAILED_TO_SEND ){
					callbackSendChatMessage.doInBackground(context, entity);
				}
				
			}
			return null;
		}

		@Override
		public void onTaskCompleted(Context context, Void result) { 
			validateAndKillSelf();
		}
	};
	
	AsyncCallbacks<Void, Void> callbackChatSync = new AsyncCallbacks<Void, Void>() {

		@Override
		public Void doInBackground(Context context, Void input) {
			APIManager.syncChatMessages(context);
			return null;
		}

		@Override
		public void onTaskCompleted(Context context, Void result) {		}
	};
	public synchronized void validateAndKillSelf(){
		mTaskCounter--;
		if( mTaskCounter == 0 && needToStopSelf ){
			mHandler.postDelayed(killRunnable, 60000);
		}
	}
	
	Runnable killRunnable = new Runnable() {
		
		@Override
		public void run() {
			stopSelf();
		}
	};
	
	public void updateSentStatus(Context context, int status, long id){
		Uri updateRecord = UnifiedInboxEntity.getContentUri(context).buildUpon().appendPath(String.valueOf(id)).build();
		ContentValues values = new ContentValues();
		values.put(UnifiedInboxEntity.STATUS, status);
		int rows = context.getContentResolver().update(updateRecord, values, null, null);
		if( rows > 0){
			context.getContentResolver().notifyChange(updateRecord, null);
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(MoEHelper.TAG, "Chat Service committed suicide.. no one was using it");
		super.onDestroy();
	}
}
