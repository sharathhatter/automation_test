package com.bigbasket.mobileapp.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public class AppNotSupportedDialog extends DialogFragment {

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
        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.updateDialogTitle)
                .content(forceUpdateMsg)
                .positiveText(R.string.update)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        UIUtil.openPlayStoreLink(getActivity());
                        getActivity().finish();
                    }
                })
                .build();
    }
}
