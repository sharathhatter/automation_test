package com.bigbasket.mobileapp.view.uiv3;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;

import java.util.ArrayList;

/**
 * Created by jugal on 12/1/15.
 */
public class ReferralTCDialog extends DialogFragment {

    private Activity context;
    private Typeface faceRobotoRegular;
    private ArrayList<String> termAndConditionArrayList;

    public ReferralTCDialog() {
    }

    public ReferralTCDialog(Activity context, Typeface faceRobotoRegular,
                            ArrayList<String> termAndConditionArrayList) {
        this.context = context;
        this.faceRobotoRegular = faceRobotoRegular;
        this.termAndConditionArrayList = termAndConditionArrayList;
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
            txtDialogTitle.setTypeface(faceRobotoRegular);
            LayoutInflater inflater = context.getLayoutInflater();
            LinearLayout layoutInnerTC = (LinearLayout) view.findViewById(R.id.layoutInnerTC);
            for(String termAndCondition : termAndConditionArrayList){
                View bulletView = inflater.inflate(R.layout.uiv3_bullet_txt, null);
                TextView txtBullet = (TextView) bulletView.findViewById(R.id.txtBullet);
                String prefix = ". ";
                Spannable spannableBullet = new SpannableString(prefix + termAndCondition);
                spannableBullet.setSpan(new StyleSpan(Typeface.BOLD), 0, prefix.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtBullet.setText(spannableBullet);

                //txtBullet.setText(". "+termAndCondition);
                layoutInnerTC.addView(bulletView);
            }
        }
    }
}
