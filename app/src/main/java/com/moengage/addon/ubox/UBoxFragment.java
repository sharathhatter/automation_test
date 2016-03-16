/* Copyright (C) MoEngage, Inc - All Rights Reserved
*  This file is subject to the terms and conditions defined in
*  file 'LICENSE.txt', which is part of this source code package.
*/
package com.moengage.addon.ubox;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moe.pushlibrary.providers.MoEDataContract;
import com.moe.pushlibrary.utils.MoEHelperConstants;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.moe.pushlibrary.MoEHelper.APP_DEBUG;
import static com.moe.pushlibrary.MoEHelper.TAG;

/**
 * @author MoEngage (abhishek@moengage.com)
 * @version 1.0
 * @since 3.0.0
 */
public class UBoxFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_CHAT = 178;
    public UBoxAdapter mAdapter;

    EditText mInputMessage;
    ListView mListView;
    ImageButton mSend;
    TextView mHelperText;
    View mProgress;
    TextView mNote;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ubox, container, false);
        NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(MoEHelperConstants.NOTIFICATION_CHAT);

        getActivity().startService(new Intent(getActivity().getApplicationContext(), UBoxMessenger.class));

        if (APP_DEBUG) Log.d(TAG, "Starting messenger");

        mListView = (ListView) view.findViewById(R.id.listChat);
        mInputMessage = (EditText) view.findViewById(R.id.inputMsg);
        mSend = (ImageButton) view.findViewById(R.id.btnSend);
        mHelperText = (TextView) view.findViewById(R.id.helperText);
        mNote = (TextView) view.findViewById(R.id.messageNote);

        mProgress = view.findViewById(R.id.progressBar);
        mHelperText.setVisibility(TextView.VISIBLE);
        mHelperText.setText(R.string.txt_loading);

        mSend.setEnabled(false);
        mSend.setOnClickListener(mSendMsgListener);

        mAdapter = new UBoxAdapter(getActivity().getApplicationContext(), null, mAdapterCallbacks);
        mListView.setAdapter(mAdapter);
        loader = new CustomCursorLoader(getActivity().getApplicationContext());

        //check for any bundle arguments that was passed
        Bundle args = getArguments();
        if (null != args && args.containsKey(BUNDLE_EXTRA_PREFILL_TEXT)) {
            mInputMessage.setText(args.getString(BUNDLE_EXTRA_PREFILL_TEXT));
        }
        return view;
    }

    public static final String BUNDLE_EXTRA_PREFILL_TEXT = "prefill_text";

    ContentObserver mDataObserver = new ContentObserver(new Handler()) {

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        ;

        public void onChange(boolean selfChange, Uri uri) {
            if (getActivity() == null) {
                return;
            }
            Loader<Cursor> vLoader = getActivity().getSupportLoaderManager().getLoader(
                    LOADER_CHAT);
            if (null != vLoader) {
                if (APP_DEBUG) Log.d(TAG, "Chat Content changed");
                vLoader.onContentChanged();
            }
        }
    };

    View.OnClickListener mSendMsgListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (APP_DEBUG) Log.d(TAG, "Send message");
            String msg = mInputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            mInputMessage.setText("");

            UnifiedInboxMessage chatItem = getPreburntMessage();
            chatItem.details = msg;
            chatItem.messageType = UnifiedInboxMessage.MSG_TYPE_TEXT;

            Message payload = Message.obtain(null, UBoxMessenger.MSG_SEND_CHAT);
            payload.obj = chatItem;

            sendMessage(payload);
        }
    };

    protected UnifiedInboxMessage getPreburntMessage() {
        UnifiedInboxMessage chatItem = new UnifiedInboxMessage();
        chatItem.author = UnifiedInboxMessage.AUTHOR_USER;
        chatItem.gtime = System.currentTimeMillis();
        chatItem.msgTtl = chatItem.gtime + +7776000000L;
        chatItem.setTimestamp(chatItem.gtime);
        return chatItem;
    }

    @Override
    public void onStart() {
        super.onStart();
        doBindService();

        getActivity().getContentResolver().registerContentObserver(
                MoEDataContract.UnifiedInboxEntity.getContentUri(getActivity().getApplicationContext()),
                true, mDataObserver);

        getActivity().getSupportLoaderManager().initLoader(LOADER_CHAT, null, this);

        MoEHelper.getInstance(getActivity()).setUserPresentInChat(true);

        if (isNetworkAvailable()) {
            isConnected(true);
        } else {
            isConnected(false);
        }
        getActivity().registerReceiver(networkChangeReceiver, mConnectivityFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(networkChangeReceiver);
        MoEHelper.getInstance(getActivity()).setUserPresentInChat(false);
        doUnbindService();

        getActivity().getContentResolver().unregisterContentObserver(mDataObserver);
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_CHAT);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return loader;
    }

    CustomCursorLoader loader = null;

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (null == cursor || cursor.getCount() == 0) {
            mListView.setVisibility(ListView.GONE);
            mHelperText.setVisibility(TextView.VISIBLE);
            mHelperText.setText(R.string.txt_empty_chat);
        } else {
            mListView.setVisibility(ListView.VISIBLE);
            mHelperText.setVisibility(TextView.GONE);
            mListView.setSelection(mAdapter.getCount() - 1);
        }
        mProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }

    static class CustomCursorLoader extends CursorLoader {
        public Context mContext;

        public CustomCursorLoader(Context context) {
            super(context);
            mContext = context;
        }

        public final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = mContext.getContentResolver().query(
                    MoEDataContract.UnifiedInboxEntity.getContentUri(mContext),
                    MoEDataContract.UnifiedInboxEntity.PROJECTION, null, null,
                    MoEDataContract.UnifiedInboxEntity.DEFAULT_SORT_ORDER);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.setNotificationUri(mContext.getContentResolver(),
                        MoEDataContract.UnifiedInboxEntity.getContentUri(mContext));
                cursor.registerContentObserver(mObserver);

            }
            return cursor;
        }
    }

    public static final int READ_REQUEST_CODE = 82;

    public ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mIsBound = true;
            mBindInProgress = false;

            sendMessage(Message.obtain(null,
                    UBoxMessenger.MSG_REGISTER_CLIENT));

            if (null != pendingQueue && pendingQueue.size() > 0) {
                int length = pendingQueue.size();
                for (; length >= 0; length--) {
                    // intentionally checking for change in service connectivity
                    if (!mIsBound)
                        break;
                    Runnable exec = pendingQueue.poll();
                    if (null != exec) {
                        exec.run();
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    public void doBindService() {
        mBindInProgress = true;
        getActivity().bindService(new Intent(getActivity(), UBoxMessenger.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            UBoxMessenger.MSG_UNREGISTER_CLIENT);
                    mService.send(msg);
                } catch (RemoteException e) {
                    Log.e(MoEHelper.TAG, "UnifiedInBoxActivity: doUnbindService", e);
                }
            }
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    /*
    * Package default
    * */
    public void sendMessage(final Message msg) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    mService.send(msg);
                    return;
                } catch (RemoteException e) {
                    Log.e(MoEHelper.TAG, "UnifiedInBoxActivity: sendMessage", e);
                }
            }
        } else if (!mBindInProgress) {
            doBindService();
        }
        pendingQueue.add(new Runnable() {

            @Override
            public void run() {
                sendMessage(msg);
            }
        });
    }

    public ConcurrentLinkedQueue<Runnable> pendingQueue = new ConcurrentLinkedQueue<Runnable>();

    boolean mBindInProgress = false;

    public void isConnected(boolean state) {
        if (state) {
            mNote.setVisibility(View.GONE);
            mInputMessage.setEnabled(true);
        } else {
            mNote.setVisibility(View.VISIBLE);
            mNote.setText(R.string.state_no_connection);
            mInputMessage.setEnabled(false);
        }
    }

    public BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                isConnected(isNetworkAvailable());
            }
        }
    };

    public IntentFilter mConnectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    Messenger mService = null;
    boolean mIsBound;


    public interface UBoxAdapterCallbacks {

        void retryFailedMessage(long messageId);

        void startActivity(Intent finalIntent);

    }

    public UBoxAdapterCallbacks mAdapterCallbacks = new UBoxAdapterCallbacks() {
        @Override
        public void retryFailedMessage(long messageId) {
            Message msg = Message.obtain(null, UBoxMessenger.MSG_RETRY, messageId);
            sendMessage(msg);
        }

        @Override
        public void startActivity(Intent finalIntent) {
            getActivity().startActivity(finalIntent);
        }

    };
}
