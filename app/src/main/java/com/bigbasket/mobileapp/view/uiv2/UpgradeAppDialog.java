package com.bigbasket.mobileapp.view.uiv2;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public class UpgradeAppDialog extends DialogFragment {

    private String upgradeMsg;

    public UpgradeAppDialog() {
    }

    public static UpgradeAppDialog newInstance(String upgradeMsg) {
        UpgradeAppDialog upgradeAppDialog = new UpgradeAppDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.UPGRADE_MSG, upgradeMsg);
        upgradeAppDialog.setArguments(bundle);
        return upgradeAppDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.upgradeMsg = getArguments().getString(Constants.UPGRADE_MSG);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.updateDialogTitle)
                .content(!TextUtils.isEmpty(upgradeMsg) ? upgradeMsg : getActivity().getString(R.string.appUpdatedMsg))
                .positiveText(R.string.update)
                .negativeText(R.string.cancel)
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
