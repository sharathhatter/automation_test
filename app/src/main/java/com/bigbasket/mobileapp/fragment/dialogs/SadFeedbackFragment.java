package com.bigbasket.mobileapp.fragment.dialogs;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.service.BBMessenger;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.AbstractDialogFragment;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.crashlytics.android.Crashlytics;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moengage.addon.ubox.UBoxMessenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SadFeedbackFragment extends AbstractDialogFragment {

    private ProgressBar progressBar;
    private Dialog alertDialog;
    private TextView txtErrorRetry;
    private EditText editTextComments;
    private boolean isResendReq = false;
    private Spinner feedbackSpinner;
    private Messenger mService = null;
    private long msgId;
    private boolean isOperationGoingOn = false;
    Handler mStatusCallbackHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == BBMessenger.MSG_SEND_STATUS) {
                handleResult(msg.arg1);
                msgId = msg.arg2;
                return true;
            }
            return false;
        }
    });
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            sendMessage(mStatusCallbackHandler.obtainMessage(UBoxMessenger.MSG_REGISTER_CLIENT, mStatusCallbackHandler));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public SadFeedbackFragment() {

    }

    public static SadFeedbackFragment newInstance() {
        return new SadFeedbackFragment();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        alertDialog = new Dialog(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.uiv3_sad_feedback_layout, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FontHolder fontHolder = FontHolder.getInstance(getActivity());
        Typeface faceRobotoRegular = fontHolder.getFaceRobotoRegular();
        int color = ContextCompat.getColor(getActivity(), R.color.uiv3_primary_text_color);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarMain);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.tell));
            toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
            setHasOptionsMenu(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIUtil.updateRatingPref(getContext(), false);
                        dismiss();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
            });
        }

        feedbackSpinner = (Spinner) view.findViewById(R.id.spinnerFeedback);
        editTextComments = (EditText) view.findViewById(R.id.editTextComments);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtErrorRetry = (TextView) view.findViewById(R.id.txtErrorRetry);

        List<String> list = Arrays.asList(getResources().getStringArray(R.array.feedback_reasons));
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        List<String> finalList = new ArrayList<>(list);
        finalList.add(0, getString(R.string.choose_category));

        BBArrayAdapter bbArrayAdapter = new BBArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, finalList,
                faceRobotoRegular, color, color);
        feedbackSpinner.setAdapter(bbArrayAdapter);

        setCancelable(false);

        alertDialog.setContentView(view);
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        view.findViewById(R.id.btnSubmitFeedBack).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard(getContext(), editTextComments);
                if (!isOperationGoingOn) {
                    if (DataUtil.isInternetAvailable(getContext())) {
                        isOperationGoingOn = true;
                        if (!isResendReq) {
                            if (feedbackSpinner.getSelectedItem().toString().equalsIgnoreCase(getString(R.string.choose_category))) {
                                showToast(getString(R.string.select_reason_to_proceed));
                            } else {
                                if (txtErrorRetry.isShown())
                                    txtErrorRetry.setVisibility(View.GONE);
                                String msg;
                                if (!UIUtil.isEmpty(editTextComments.getText().toString())) {
                                    msg = getString(R.string.preamble_feedback) + feedbackSpinner.getSelectedItem().toString() + "\n" + editTextComments.getText().toString();
                                } else {
                                    msg = getString(R.string.preamble_feedback) + feedbackSpinner.getSelectedItem().toString();
                                }
                                UnifiedInboxMessage chatItem = getPreburntMessage();
                                chatItem.details = msg;
                                chatItem.messageType = 1;
                                Message payload = mStatusCallbackHandler.obtainMessage(UBoxMessenger.MSG_SEND_CHAT, mStatusCallbackHandler);
                                payload.obj = chatItem;
                                sendMessage(payload);
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        } else {
                            resendMessage(msgId);
                        }
                    } else {
                        showToast(getString(R.string.connectionOffline));
                    }
                }
            }
        });

        return alertDialog;
    }

    private void resendMessage(long msgId) {
        txtErrorRetry.setVisibility(View.GONE);
        Message msg = mStatusCallbackHandler.obtainMessage(UBoxMessenger.MSG_RETRY, msgId);
        sendMessage(msg);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void handleResult(int status) {
        switch (status) {
            case UnifiedInboxMessage.STATUS_SENDING:
                isOperationGoingOn = true;
                if (progressBar != null && txtErrorRetry != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    txtErrorRetry.setVisibility(View.GONE);
                }
                break;
            case UnifiedInboxMessage.STATUS_FAILED_TO_SEND:
                isOperationGoingOn = false;
                if (progressBar != null && txtErrorRetry != null) {
                    progressBar.setVisibility(View.GONE);
                    txtErrorRetry.setVisibility(View.VISIBLE);
                }
                isResendReq = true;
                break;
            case UnifiedInboxMessage.STATUS_SENT:
                isOperationGoingOn = false;
                msgId = 0L;
                isResendReq = false;
                if (progressBar != null && alertDialog != null && txtErrorRetry != null) {
                    progressBar.setVisibility(View.GONE);
                    txtErrorRetry.setVisibility(View.GONE);
                    showToast(getString(R.string.feedback_success));
                    try {
                        if (editTextComments != null) {
                            hideKeyboard(getContext(), editTextComments);
                        }
                        doUnbindService();
                        UIUtil.updateRatingPref(getContext(), false);
                        dismiss();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
                break;
            default:
                break;
        }
    }

    private UnifiedInboxMessage getPreburntMessage() {
        UnifiedInboxMessage chatItem = new UnifiedInboxMessage();
        chatItem.author = "User";
        chatItem.gtime = System.currentTimeMillis();
        chatItem.msgTtl = chatItem.gtime + 7776000000L;
        chatItem.setTimestamp(chatItem.gtime);
        return chatItem;
    }

    @Override
    public void onStop() {
        doUnbindService();
        super.onStop();
    }

    private void doBindService() {
        if (getContext() == null) return;
        getContext().bindService(new Intent(getContext(), BBMessenger.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        if (this.mService != null) {
            try {
                Message e = mStatusCallbackHandler.obtainMessage(UBoxMessenger.MSG_UNREGISTER_CLIENT, mStatusCallbackHandler);
                this.mService.send(e);
            } catch (RemoteException var2) {
                Crashlytics.logException(var2);
            }
        }
        if (getContext() == null) return;
        try {
            getContext().unbindService(serviceConnection);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onStart() {
        doBindService();
        super.onStart();
    }

    public void sendMessage(final Message msg) {
        if (mService != null) {
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            this.doBindService();
        }
    }

    @Override
    protected String getScreenTag() {
        return TrackEventkeys.FEEDBACK_DIALOG_SCREEN;
    }
}