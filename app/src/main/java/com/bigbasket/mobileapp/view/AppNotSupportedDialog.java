package com.bigbasket.mobileapp.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.UIUtil;

public class AppNotSupportedDialog extends DialogFragment {

    public AppNotSupportedDialog() {
    }

    public static AppNotSupportedDialog newInstance() {
        return new AppNotSupportedDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.update);
        builder.setMessage(R.string.appOutDatedMsg)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.openPlayStoreLink(getActivity());
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    getActivity().finish();
                }
                return true;
            }
        });
        return alertDialog;
    }
}
