package com.bigbasket.mobileapp.view.uiv3.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;

public abstract class BBFullScreenDialog extends DialogFragment {

    protected BaseActivity baseActivity;

    public BBFullScreenDialog(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public abstract void setContentArea(LinearLayout base);

    public void OnCloseButtonClicked() {

    }

    public void OnOkButtonClicked() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BBFullScreenDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View decorView = baseActivity.getWindow().getDecorView();
            dialog.getWindow().setFlags(decorView.getWidth(), decorView.getHeight());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_dialog_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            LinearLayout base = (LinearLayout) view.findViewById(R.id.layoutDialogContent);
            ImageView imgDialogOk = (ImageView) view.findViewById(R.id.imgDialogOk);
            ImageView imgDialogDismiss = (ImageView) view.findViewById(R.id.imgDialogDismiss);
            imgDialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnOkButtonClicked();
                }
            });
            imgDialogDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnCloseButtonClicked();
                }
            });
            setContentArea(base);
        }
    }
}
