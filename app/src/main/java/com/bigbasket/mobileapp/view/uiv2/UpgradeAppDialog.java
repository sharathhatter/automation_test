package com.bigbasket.mobileapp.view.uiv2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.updateDialogTitle)
                .setMessage(!TextUtils.isEmpty(upgradeMsg) ? upgradeMsg : getActivity().getString(R.string.appUpdatedMsg))
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.openPlayStoreLink(getActivity());
                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }
}
