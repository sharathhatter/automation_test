package com.bigbasket.mobileapp.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public class AppNotSupportedDialog extends AppCompatDialogFragment {

    private String upgradeMsg;
    private String latestAppVersion;

    public AppNotSupportedDialog() {
    }

    public static AppNotSupportedDialog newInstance(String upgradeMsg, String latestAppVersion) {
        AppNotSupportedDialog upgradeAppDialog = new AppNotSupportedDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.UPGRADE_MSG, upgradeMsg);
        bundle.putString(Constants.LATEST_APP_VERSION, latestAppVersion);
        upgradeAppDialog.setArguments(bundle);
        return upgradeAppDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.upgradeMsg = getArguments().getString(Constants.UPGRADE_MSG);
        this.latestAppVersion = getArguments().getString(Constants.LATEST_APP_VERSION);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String forceUpdateMsg = getString(R.string.appOutDatedMsg1);
        if (!TextUtils.isEmpty(latestAppVersion)) {
            forceUpdateMsg += " (" + latestAppVersion + "). ";
        } else {
            forceUpdateMsg += ". ";
        }
        forceUpdateMsg += getString(R.string.appOutDatedMsg2) + " ";
        if (!TextUtils.isEmpty(upgradeMsg)) {
            forceUpdateMsg += getString(R.string.whyUpdate) + " " + upgradeMsg;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.updateDialogTitle)
                .setMessage(forceUpdateMsg)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.openPlayStoreLink(getActivity());
                        getActivity().finish();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                        return true;
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }
}
