package com.payu.payuui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

/**
 * Created by manu on 23/12/15.
 */
public class TransactionDialogFragment extends AppCompatDialogFragment {

    private static final String ARG_REQUEST_CODE = "arg_request_code";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_POSITIVE_BUTTON_TEXT = "arg_positive_button_text";
    private static final String ARG_NEGATIVE_BUTTON_TEXT = "arg_negative_button_text";
    Bundle detailsBundle;
    private TransactionDialogListener transactionDialogListener;
    private int requestCode;

    public TransactionDialogFragment() {
    }

    public static TransactionDialogFragment newInstance(String message, int requestCode, String primaryTxt, String secondaryTxt) {
        TransactionDialogFragment transactionDialogFragment = new TransactionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MESSAGE, message);
        bundle.putString(ARG_POSITIVE_BUTTON_TEXT, primaryTxt);
        bundle.putString(ARG_NEGATIVE_BUTTON_TEXT, secondaryTxt);
        bundle.putInt(ARG_REQUEST_CODE, requestCode);
        transactionDialogFragment.setArguments(bundle);
        return transactionDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            detailsBundle = bundle;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        requestCode = args.getInt(ARG_REQUEST_CODE, 0);

        String message = args.getString(ARG_MESSAGE);
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        String positiveButtonText = args.getString(ARG_POSITIVE_BUTTON_TEXT);
        if (!TextUtils.isEmpty(positiveButtonText)) {
            builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (transactionDialogListener != null) {
                        transactionDialogListener.onDialogConfirmed(requestCode, true);
                    }
                }
            });
        }
        String negativeButtonText = args.getString(ARG_NEGATIVE_BUTTON_TEXT);
        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (transactionDialogListener != null) {
                        transactionDialogListener.onDialogConfirmed(requestCode, false);
                    }
                }
            });
        }
        builder.setCancelable(false);
        setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getTargetFragment() instanceof TransactionDialogListener) {
            transactionDialogListener = (TransactionDialogListener) getTargetFragment();
        } else if (activity instanceof TransactionDialogListener) {
            transactionDialogListener = (TransactionDialogListener) activity;
        }
    }

    public interface TransactionDialogListener {
        void onDialogConfirmed(int reqCode, boolean isPositive);
    }

}
