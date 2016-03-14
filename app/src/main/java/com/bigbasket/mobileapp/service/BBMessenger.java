package com.bigbasket.mobileapp.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.moengage.addon.ubox.UBoxMessenger;


public class BBMessenger extends UBoxMessenger {

    public static final int MSG_SEND_STATUS = 100;
    private Handler callBackHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    public void handleIncomingMessages(Message msg) {
        switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                callBackHandler = (Handler) msg.obj;
                break;
            case MSG_UNREGISTER_CLIENT:
                callBackHandler = null;
                break;
            default:
                break;
        }
        super.handleIncomingMessages(msg);
    }

    @Override
    public void updateSentStatus(Context context, int status, long id) {
        super.updateSentStatus(context, status, id);
        if (callBackHandler != null) {
            Message msg = callBackHandler.obtainMessage(MSG_SEND_STATUS);
            msg.arg1 = status;
            msg.arg2 = (int) id;
            callBackHandler.sendMessage(msg);
        }
    }
}