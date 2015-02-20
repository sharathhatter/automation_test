package com.bigbasket.mobileapp.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
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
        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.update)
                .content(R.string.appOutDatedMsg)
                .positiveText(R.string.update)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        UIUtil.openPlayStoreLink(getActivity());
                    }
                })
                .build();
    }
}
