package com.bigbasket.mobileapp.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.UIUtil;

public class AppNotSupportedDialog<T> {

    private T ctx;

    public AppNotSupportedDialog(T ctx) {
        this.ctx = ctx;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(((ActivityAware) ctx).getCurrentActivity());
        builder.setTitle(R.string.update);
        builder.setMessage(R.string.appOutDatedMsg)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.openPlayStoreLink(((ActivityAware) ctx).getCurrentActivity());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        (((ActivityAware) ctx).getCurrentActivity()).finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    (((ActivityAware) ctx).getCurrentActivity()).finish();
                }
                return true;
            }
        });
        alertDialog.show();
    }
}
