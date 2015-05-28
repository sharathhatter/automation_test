package com.bigbasket.mobileapp.handler;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

public class OnDialogShowListener implements DialogInterface.OnShowListener {
    @Override
    public void onShow(DialogInterface dialog) {
        if (dialog instanceof AlertDialog) {
            Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK);
            }
        }
    }
}
