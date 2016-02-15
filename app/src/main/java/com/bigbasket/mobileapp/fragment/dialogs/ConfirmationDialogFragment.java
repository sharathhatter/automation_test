package com.bigbasket.mobileapp.fragment.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;


public class ConfirmationDialogFragment extends AppCompatDialogFragment {

    private static final String ARG_REQUEST_CODE = "arg_request_code";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_POSITIVE_BUTTON_TEXT = "arg_positive_button_text";
    private static final String ARG_NEGATIVE_BUTTON_TEXT = "arg_negative_button_text";
    private static final String ARG_IS_CANCELLABLE = "arg_is_cancellable";
    private static final String ARG_DATA = "arg_data";

    public interface ConfirmationDialogCallback {
        void onDialogConfirmed(int reqCode, Bundle data, boolean isPositive);

        void onDialogCancelled(int reqCode, Bundle data);
    }

    public static ConfirmationDialogFragment newInstance(int requestCode,
                                                         String message,
                                                         String positiveButtonText,
                                                         boolean isCancellable) {
        return newInstance(null, requestCode, null, message, positiveButtonText,
                null, null, isCancellable);
    }

    public static ConfirmationDialogFragment newInstance(int requestCode,
                                                         @Nullable String title, String message,
                                                         String positiveButtonText,
                                                         @Nullable String negativeButtonText,
                                                         boolean isCancellable) {
        return newInstance(requestCode, title, message, positiveButtonText,
                negativeButtonText, null, isCancellable);
    }

    public static ConfirmationDialogFragment newInstance(int requestCode,
                                                         @Nullable String title, String message,
                                                         String positiveButtonText,
                                                         @Nullable String negativeButtonText,
                                                         @Nullable Bundle data,
                                                         boolean isCancellable) {
        return newInstance(null, requestCode, title, message, positiveButtonText,
                negativeButtonText, data, isCancellable);
    }

    public static ConfirmationDialogFragment newInstance(@Nullable Fragment fragment, int requestCode,
                                                         @Nullable String title, String message,
                                                         String positiveButtonText,
                                                         @Nullable String negativeButtonText,
                                                         @Nullable Bundle data,
                                                         boolean isCancellable) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        args.putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        args.putBoolean(ARG_IS_CANCELLABLE, isCancellable);
        args.putBundle(ARG_DATA, data);
        ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
        dialogFragment.setArguments(args);
        if (fragment != null) {
            dialogFragment.setTargetFragment(fragment, requestCode);
        }
        return dialogFragment;
    }

    private ConfirmationDialogCallback confirmationDialogCallback;
    private int requestCode;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getTargetFragment() instanceof ConfirmationDialogCallback) {
            confirmationDialogCallback = (ConfirmationDialogCallback) getTargetFragment();
        } else if (activity instanceof ConfirmationDialogCallback) {
            confirmationDialogCallback = (ConfirmationDialogCallback) activity;
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        requestCode = args.getInt(ARG_REQUEST_CODE, 0);
        String title = args.getString(ARG_TITLE);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        String message = args.getString(ARG_MESSAGE);
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        String positiveButtonText = args.getString(ARG_POSITIVE_BUTTON_TEXT);
        if (!TextUtils.isEmpty(positiveButtonText)) {
            builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (confirmationDialogCallback != null) {
                        confirmationDialogCallback.onDialogConfirmed(
                                requestCode, args.getBundle(ARG_DATA), true);
                    }
                }
            });
        }
        String negativeButtonText = args.getString(ARG_NEGATIVE_BUTTON_TEXT);
        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (confirmationDialogCallback != null) {
                        confirmationDialogCallback.onDialogConfirmed(
                                requestCode, args.getBundle(ARG_DATA), false);
                    }
                }
            });
        }
        boolean isCancelable = args.getBoolean(ARG_IS_CANCELLABLE, false);
        builder.setCancelable(isCancelable);
        setCancelable(isCancelable);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(isCancelable);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (confirmationDialogCallback != null) {
            Bundle args = getArguments();
            confirmationDialogCallback.onDialogCancelled(requestCode,
                    args != null ?args.getBundle(ARG_DATA) : null);
        }
    }
}
