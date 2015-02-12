package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

/**
 * Created by jugal on 12/1/15.
 */
public class TermAndConditionDialog extends DialogFragment {

    private ArrayList<String> termAndConditionArrayList;

    public TermAndConditionDialog() {
    }

    public static TermAndConditionDialog newInstance(ArrayList<String> termAndConditionArrayList) {
        TermAndConditionDialog termAndConditionDialog = new TermAndConditionDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Constants.TERMS_AND_COND, termAndConditionArrayList);
        termAndConditionDialog.setArguments(bundle);
        return termAndConditionDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.termAndConditionArrayList = getArguments().getStringArrayList(Constants.TERMS_AND_COND);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_referral_tc, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
            txtDialogTitle.setTypeface(BaseActivity.faceRobotoRegular);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            LinearLayout layoutInnerTC = (LinearLayout) view.findViewById(R.id.layoutInnerTC);
            for (String termAndCondition : termAndConditionArrayList) {
                View bulletView = inflater.inflate(R.layout.uiv3_bullet_txt, null);
                TextView txtBullet = (TextView) bulletView.findViewById(R.id.txtBullet);
                txtBullet.setText(termAndCondition);
                layoutInnerTC.addView(bulletView);
            }
        }
    }
}
